import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class Main {

	// private static final String INPUT_DATA_DIRECTORY = "/dreampost";
	// private static final String OUTPUT_DATA_CROP_DIRECTORY = INPUT_DATA_DIRECTORY + "/dreampost_crop";
	// private static final String LOG_DIRECTORY = INPUT_DATA_DIRECTORY + "/dreampost_crop_logs";

	private static final String INPUT_DATA_DIRECTORY = "C:/Users/lena2/OneDrive/사진/world_vision";
	private static final String OUTPUT_DATA_CROP_DIRECTORY = INPUT_DATA_DIRECTORY + "/dreampost_crop";
	private static final String LOG_DIRECTORY = INPUT_DATA_DIRECTORY + "/dreampost_crop_logs";

	private static int successCount = 0;
	private static int failedCount = 0;
	static StringBuffer stringBuffer = new StringBuffer();

	public static void main(String[] args) {

		// INPUT 폴더가 없을 경우
		if(!new File(INPUT_DATA_DIRECTORY).exists()) {
			throw new RuntimeException("Input data directory does not exist");
		}

		LocalDateTime start = LocalDateTime.now();

		try {
			Files.createDirectories(Paths.get(OUTPUT_DATA_CROP_DIRECTORY));
		} catch (IOException ex) {
			stringBuffer.append("[")
				.append(LocalDateTime.now())
				.append("] ")
				.append(OUTPUT_DATA_CROP_DIRECTORY)
				.append(": IOException Failed\n(")
				.append(ex.getCause())
				.append(")");
		}

		File dir = new File(INPUT_DATA_DIRECTORY);
		List<String> files = Arrays.stream(Objects.requireNonNull(dir.list()))
			.filter(e -> !e.contains("dreampost"))
			.collect(Collectors.toList());

		files.parallelStream().forEach(e -> {
			// 폴더 유/무 확인 및 생성
			File inputDir = new File(INPUT_DATA_DIRECTORY, e);
			if(!inputDir.isFile()) {
				File outDir = new File(OUTPUT_DATA_CROP_DIRECTORY, e);
				try {
					Files.createDirectories(outDir.toPath());
				} catch (IOException ex) {
					stringBuffer.append("[")
						.append(LocalDateTime.now())
						.append("] ")
						.append(OUTPUT_DATA_CROP_DIRECTORY)
						.append("/")
						.append(e)
						.append(": IOException Failed\n(")
						.append(ex.getCause())
						.append(")");
				}

				// 이미지 병렬 처리
				File inputImage = new File(String.valueOf(inputDir.toPath()));
				List<String> crop = Arrays.stream(Objects.requireNonNull(inputImage.list()))
					.filter(x -> x.endsWith(".jpg"))
					.collect(Collectors.toList());

				crop.parallelStream().forEach(imgName -> {
					System.out.println(outDir.toPath()+"/"+imgName);
					imageCrop(inputDir.toPath() +"/"+ imgName, outDir.toPath()+"/"+imgName);
				});
			}
		});

		String finish = Duration.between(start, LocalDateTime.now()).getSeconds() + "s";
		System.out.println("Finish Time: "+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) + " / Finish Time: " + finish + " / Success Count: " + successCount + " / Failed Count: " + failedCount + " / Total Count: " + (successCount + failedCount));
		if(stringBuffer.length() == 0) {
			stringBuffer.append("Finish Time: ")
				.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")))
				.append(" / Finish Time: ")
				.append(finish)
				.append(" / Total Count: ")
				.append(successCount);
		}
		createErrorLog(stringBuffer);
	}

	static void imageCrop(String inputDataDirectory, String outputDataDirectory) {

		// 크롭된 이미지 있을 경우 다음 이미지로 이동
		if(new File(outputDataDirectory).exists())
			return;

		BufferedImage originalImg;
		try {
			originalImg = ImageIO.read(new File(inputDataDirectory));
			int height = originalImg.getHeight();
			int width = originalImg.getWidth();

			BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);

			Graphics2D g2d = rotatedImage.createGraphics();

			// 회전 변환 설정
			AffineTransform at = new AffineTransform();
			at.translate(height / 2, width / 2);
			at.rotate(Math.PI / 2);
			at.translate(-width / 2, -height / 2);
			g2d.setTransform(at);
			g2d.drawImage(originalImg, 0, 0, null);
			g2d.dispose();

			// 3338 x 4676 기준 이미지 자름
			BufferedImage SubImg = rotatedImage.getSubimage(120, 2750, 3080, 1800);

			File outputfile = new File(outputDataDirectory);
			ImageIO.write(SubImg, "jpg", outputfile);
			successCount ++;
		} catch (IOException ex) {
			failedCount ++;
			ex.printStackTrace();
			stringBuffer.append("[")
				.append(LocalDateTime.now())
				.append("] ")
				.append(inputDataDirectory)
				.append(": IOException Failed\n(")
				.append(ex.getCause())
				.append(")");
		} catch (NullPointerException ex) {
			failedCount ++;
			ex.printStackTrace();
			stringBuffer.append("[")
				.append(LocalDateTime.now())
				.append("] ")
				.append(inputDataDirectory)
				.append(": NullPointerException Failed\n(")
				.append(ex.getCause())
				.append(")");
		} catch (Exception ex) {
			failedCount ++;
			ex.printStackTrace();
			stringBuffer.append("[")
				.append(LocalDateTime.now())
				.append("] ")
				.append(inputDataDirectory)
				.append(": Exception Failed\n(")
				.append(ex.getCause())
				.append(")");
		}
	}

	public static void createErrorLog(StringBuffer stringBuffer) {
		Path directoryPath = Paths.get(LOG_DIRECTORY);
		try {
			Files.createDirectories(directoryPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		File file = new File(LOG_DIRECTORY, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH-mm-ss")) + "-log");
		try {
			FileWriter fw = new FileWriter(file) ;
			fw.write(stringBuffer.toString());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}