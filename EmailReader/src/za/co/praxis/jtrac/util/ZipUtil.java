package za.co.praxis.jtrac.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility to Zip and Unzip nested directories recursively.
 */
public class ZipUtil {

	// ------------------------------------------------------------------------ INPUTSTREAM

	/**
	 * Create a ZipArchiveOutputStream. 
	 * @param zipFile: The zip file of the archive to create.
	 * @throws IOException: If anything goes wrong
	 */
	public static ZipArchiveOutputStream createZipArchiveOutputStream(File zipFile) throws IOException {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		ZipArchiveOutputStream tOut = null;
		//
		fOut = new FileOutputStream(zipFile);
		bOut = new BufferedOutputStream(fOut);
		tOut = new ZipArchiveOutputStream(bOut);
		return tOut;
	}	
	
	/**
	 * Creates a zip entry for the inputstreamin the zip.
	 * 
	 * @param zOut: The zip file's output stream
	 * @param path: The fileName for the name of the zip file entry
	 * @param inputStream: The stream to add in zip file.
	 * @throws IOException: If anything goes wrong
	 */
	public static void addInputStreamToZip(ZipArchiveOutputStream zOut, String fileName, InputStream inputStream) throws IOException {
		String entryName = fileName;
		ZipArchiveEntry zipEntry = new ZipArchiveEntry(entryName);
		zOut.putArchiveEntry(zipEntry);
		log("----- addFileToZip file "+fileName);
		IOUtils.copy(inputStream, zOut);
		zOut.closeArchiveEntry();
	}
	
	/**
	 * Close the 
	 * @param zOut
	 * @throws IOException
	 */
	public static void closeZipArchiveOutputStream(ZipArchiveOutputStream zOut) throws IOException {
		zOut.finish();
		zOut.close();
	}

	// ------------------------------------------------------------------------------ FILES
	
	/**
	 * Creates a zip file at the specified path with the contents of the specified directory. 
	 * 
	 * @param directoryPath: The path of the directory where the archive will be created.
	 * @param zipPath: The full path of the archive to create.
	 * @throws IOException: If anything goes wrong
	 */
	public static void createZip(String directoryPath, String zipPath) throws IOException {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		ZipArchiveOutputStream tOut = null;
		try {
			fOut = new FileOutputStream(new File(zipPath));
			bOut = new BufferedOutputStream(fOut);
			tOut = new ZipArchiveOutputStream(bOut);
			addFileToZip(tOut, directoryPath, "");
		} finally {
			tOut.finish();
			tOut.close();
			bOut.close();
			fOut.close();
		}
	}
	
	/**
	 * Creates a zip entry for the path specified with a name built from the
	 * base passed in and the file/directory name. If the path is a directory,
	 * a recursive call is made such that the full directory is added to the
	 * zip.
	 * 
	 * @param zOut: The zip file's output stream
	 * @param path: The filesystem path of the file/directory being added
	 * @param base: The base prefix to for the name of the zip file entry
	 * @throws IOException: If anything goes wrong
	 */
	private static void addFileToZip(ZipArchiveOutputStream zOut, String path, String base) throws IOException {
		File f = new File(path);
		String entryName = base + f.getName();
		
		if (f.isFile()) {
			
			ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);
			zOut.putArchiveEntry(zipEntry);
			
			FileInputStream fInputStream = null;
			try {
				log("----- addFileToZip file "+f.getAbsolutePath());
				fInputStream = new FileInputStream(f);
				IOUtils.copy(fInputStream, zOut);
				zOut.closeArchiveEntry();
			} finally {
				fInputStream.close();
			}
		
		} else {
			log("-------------- addFileToZip directories "+f.getAbsolutePath());
			//zOut.closeArchiveEntry();
			File[] children = f.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToZip(zOut, child.getAbsolutePath(), entryName + "/");
				}
			}
		}
	}
	
	
	/**
	 * Extract zip file at the specified destination path. 
	 * NB:archive must consist of a single root folder containing everything else
	 * 
	 * @param archivePath: path to zip file
	 * @param destinationPath: path to extract zip file to. Created if it doesn't exist.
	 */
	public static void extractZip(String archivePath, String destinationPath) {
		File archiveFile = new File(archivePath);
		File unzipDestFolder = null;
		try {
			unzipDestFolder = new File(destinationPath);
			String[] zipRootFolder = new String[] { null };
			unzipFolder(archiveFile, archiveFile.length(), unzipDestFolder, zipRootFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Unzips a zip file into the given destination directory.
	 * The archive file MUST have a unique "root" folder. 
	 * This root folder is skipped when unarchiving.
	 * 
	 * @return true if folder is unzipped correctly.
	 */    
	@SuppressWarnings("unchecked") 
	private static boolean unzipFolder(File archiveFile, long compressedSize, File zipDestinationFolder, String[] outputZipRootFolder) {
		ZipFile zipFile = null;
		
		try {
			zipFile = new ZipFile(archiveFile);
			byte[] buf = new byte[65536];
			Enumeration entries = zipFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry zipEntry = (ZipArchiveEntry) entries.nextElement();
				String name = zipEntry.getName();
				name = name.replace('\\', '/');
				/*
				// Delete the root(first) folder 
				{
					int i = name.indexOf('/');
					if (i>0) {
						outputZipRootFolder[0] = name.substring(0, i);
						name = name.substring(i + 1);
					}
				}*/
				File destinationFile = new File(zipDestinationFolder, name);
				if (name.endsWith("/")) {
					if (!destinationFile.isDirectory() && !destinationFile.mkdirs()) {
						log("Error creating temp directory:" + destinationFile.getPath());
						return false;
					}
					continue;
					
				} else if (name.indexOf('/') != -1) {
					// Create the the parent directory if it doesn't exist
					File parentFolder = destinationFile.getParentFile();
					if (!parentFolder.isDirectory()) {
						if (!parentFolder.mkdirs()) {
							log("Error creating temp directory:" + parentFolder.getPath());
							return false;
						}
					}
				}
				
				if(!destinationFile.isDirectory()){
					log("-------------- unzipFolder file "+destinationFile.getAbsolutePath());
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(destinationFile);
						int n;
						InputStream entryContent = zipFile.getInputStream(zipEntry);
						while ((n = entryContent.read(buf)) != -1) {
							if (n > 0) {
								fos.write(buf, 0, n);
							}
						}
					} finally {
						if (fos != null) {
							fos.close();
						}
					}
				}
			}
			return true;
		} catch (IOException e) {             
			log("Unzip failed:" + e.getMessage());
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					log("Error closing zip file");                 
				}             
			}         
		}           
		return false; 
	}

	private static void log(String msg) {
		System.out.println(msg);
	}
	
	
	private static String getRandomLocalFilename() {
		/*long millisec = Calendar.getInstance().getTimeInMillis();
		double randomDigit = Math.random();
        StringBuffer tempFileName = new StringBuffer(Long.toString(millisec)).append(randomDigit);
        return tempFileName.toString();*/
        // OR
        return UUID.randomUUID().toString();
	}
	
    /**
     * Method for testing zipping and unzipping. 
     * Check the result in test_compress1\bin\jUnitResources
     * 
     * @param args
     */
	/*public static void main(String[] args) throws IOException {
		
		String dirToZip = "C:\\attachments_zip\\";//"toCompress"; // Folder containing the files to compress 

		// ZIP
		createZip(dirToZip, "C:\\attachments" + File.separator + "sam.zip");
		 
	} */
	
}