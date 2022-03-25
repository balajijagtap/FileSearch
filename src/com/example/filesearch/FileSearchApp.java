package com.example.filesearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileSearchApp {

	String path;
	String regex;
	String zipFileName;
	Pattern pattern;
	List<File> files = new ArrayList<File>();

	public static void main(String[] args) {
		FileSearchApp app = new FileSearchApp();

		switch (Math.min(args.length, 3)) {
		case 0:
			System.out.println("USAGE: FileSearchApp path [regex] [zipfile]");
			return;
		case 3:
			app.setZipFileName(args[2]);
		case 2:
			app.setRegex(args[1]);
		case 1:
			app.setPath(args[0]);
		}

		app.printArgs();

		try {
			app.walkDirectory(app.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printArgs() {
		System.out.println("arguments received: ");
		System.out.println("path: " + path);
		System.out.println("regex: " + regex);
		System.out.println("zip file name: " + zipFileName);
		System.out.println("####################");
	}

	public void walkDirectory(String path) {
		File dir = new File(path);
		File[] files = dir.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				walkDirectory(file.getAbsolutePath());
			} else {
				processFile(file);
			}
		}

		if (getZipFileName() != null) {
			try {
				addFilesToZip();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void addFilesToZip() throws IOException {
		FileOutputStream fos = new FileOutputStream(getZipFileName());
		ZipOutputStream zos = new ZipOutputStream(fos);
		File dir = new File(path);
		for (File file : files) {

			String fileName = getRelativePath(file, dir);

			System.out.println("Writing '" + fileName + "' to zip file");

			File addfile = new File(fileName);
			FileInputStream fis = new FileInputStream(addfile);
			ZipEntry zipEntry = new ZipEntry(fileName);
			zos.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}

			zos.closeEntry();
			fis.close();
		}

		zos.close();
		fos.close();
	}

	private String getRelativePath(File file, File base) {
		String file_path = file.getAbsolutePath();
		String base_path = base.getAbsolutePath();
		String relative = new File(base_path).toURI().relativize(new File(file_path).toURI()).getPath();
		return relative;
	}

	public void processFile(File file) {
		if (searchFile(file)) {
			files.add(file);
		}
	}

	public boolean searchFile(File file) {
		boolean found = false;
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (scanner.hasNextLine()) {
			found = searchText(scanner.nextLine());
			if (found) {
				break;
			}
		}

		scanner.close();
		return found;
	}

	public boolean searchText(String line) {
		return (getRegex() == null) ? true : pattern.matcher(line).matches();
	}

	public void addFileToZip(File file) {
		System.out.println(file.getAbsolutePath() + " added to zip");
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}
}
