import java.util.*;
import java.util.stream.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.file.attribute.BasicFileAttributes;

public class PicOrg {

	public static String destFolder = "PicOrg/";

	public static Date getCreateDate(Path filePath) {
		BasicFileAttributes attr;
		Date createDate = null;
		try {
			attr = Files.readAttributes(filePath, BasicFileAttributes.class);

			//System.out.println("Creation date: " + attr.creationTime());
			createDate = new Date(attr.creationTime().toMillis());
		} catch (IOException e) {
			// whatever
		}
		return createDate;
	}

	public static String createDatePathFromFile(Path filePath) {
	
		Date createDate = getCreateDate(filePath);

		return createDatePath(createDate);

	}

	public static String createDatePath(Date date) {
		
		return "" + (1900+date.getYear()) + "/" + (date.getMonth() < 9 ? "0" : "") + (date.getMonth()+1) + "/";

	}

	public static String asHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();

		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		return hexString.toString();
	}

	public static void report(Map<String, List<Path>> fileMap) {

		String toPath;
		String fromPath;
		String filename;

		for (List<Path> eachList : fileMap.values()) {
			if (eachList.size() > 1) {
				// Find oldest date
				Date oldestDate = new Date();
				Path path = null;
				for (Path p : eachList) {
					Date date = getCreateDate(p);
					if (date.before(oldestDate)) {
						oldestDate = date;
						path = p;
					}
				}
				System.out.println("# Duplicates exist");
				fromPath = path.toString();
				toPath = destFolder + createDatePath(oldestDate);
				filename = path.getFileName().toString();
//				System.out.print("mv " + path.toString() + " ");
//				System.out.print(destFolder + createDatePath(oldestDate));
//				System.out.println(path.getFileName());				
			} else {
				Path p = eachList.get(0);
				fromPath = p.toString();
				toPath = destFolder + createDatePathFromFile(p);
				filename = p.getFileName().toString();
//				System.out.print("mv " + p.toString() + " ");
//				System.out.print(destFolder + createDatePathFromFile(p));
//				System.out.println(p.getFileName());	
			}
			System.out.println("mkdir -p " + toPath);
			System.out.println("mv " + fromPath + " " + toPath + filename);

		}

	}

	public static String determineHash(Path path) throws Exception {
		byte[] b = Files.readAllBytes(path);
		byte[] hash = MessageDigest.getInstance("MD5").digest(b);
		String hashKey = asHex(hash);
		return hashKey;
	}

	public static void main(String[] args) {

		String startPath = ".";

		if (args.length > 0) {
			startPath = args[0];
		} else {
			System.out.println("Usage: java PicOrg startPath");
			return;
		}

		Map<String, List<Path>> fileHash = new HashMap<String, List<Path>>();

		try (Stream<Path> paths = Files.walk(Paths.get(startPath))) {
			//			paths.forEach(System.out::println);
			for (Object each : paths.toArray()) {
				Path eachPath = (Path) each;
				//System.out.println(eachPath);
				try {
					String hashKey = determineHash(eachPath);

					if (fileHash.containsKey(hashKey)) {
						List<Path> list = fileHash.get(hashKey);
						list.add(eachPath);

					} else {
						List<Path> list = new ArrayList<Path>();
						list.add(eachPath);
						fileHash.put(hashKey, list);
					}

				} catch (Exception e) {
					// don't care'
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		report(fileHash);
	}
}
