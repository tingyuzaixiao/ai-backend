package com.train.platform.common.core.util;

import cn.hutool.core.io.FileUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

public class RLEProcessor {

	// BitInputStream实现类
	static class BitInputStream {
		private final byte[] data;
		private int bitPosition = 0;

		public BitInputStream(byte[] data) {
			this.data = data;
		}

		public int read(int bits) {
			int result = 0;
			for (int i = 0; i < bits; i++) {
				int bytePos = bitPosition / 8;
				int bitPos = 7 - (bitPosition % 8);
				int value = (data[bytePos] & 0xFF) >> bitPos & 1;
				result |= (value << (bits - 1 - i));
				bitPosition++;
			}
			return result;
		}
	}

	public static byte[] intToBinary(int[] arr) {
		byte[] result = new byte[arr.length];
		for (int i = 0; i < arr.length; i++) {
			result[i] = (byte) (arr[i] & 0xFF);
		}
		return result;
	}

	// RLE解码方法
	public static byte[] decodeRLE(byte[] rleData) {
		BitInputStream input = new BitInputStream(rleData);
		int num = input.read(32);
		int wordSize = input.read(5) + 1;
		int[] rleSizes = new int[4];
		for (int i = 0; i < 4; i++) {
			rleSizes[i] = input.read(4) + 1;
		}

		byte[] out = new byte[num];
		int i = 0;
		while (i < num) {
			int x = input.read(1);
			int idx = input.read(2);
			int runLength = input.read(rleSizes[idx]) + 1;
			int j = i + runLength;

			if (x == 1) {
				int val = input.read(wordSize);
				for (int k = i; k < j; k++) {
					out[k] = (byte) (val & 0xFF);
				}
				i = j;
			} else {
				while (i < j) {
					out[i++] = (byte) (input.read(wordSize) & 0xFF);
				}
			}
		}
		return out;
	}

	// RLE编码方法
	public static byte[] encodeRLE(byte[] data, int wordSize, int[] rleSizes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteBuffer header = ByteBuffer.allocate(8);

		// 写入头部信息
		header.putInt(data.length); // 32位数值
		header.put((byte) (wordSize - 1)); // wordSize-1 (5位)

		// 写入rleSizes (4个4位值)
		for (int size : rleSizes) {
			header.put((byte) (size - 1));
		}

		// 处理RLE编码逻辑
		StringBuilder outStr = new StringBuilder();
		int[] runs = baseRLEncode(data);

		for (int k = 0; k < runs.length; k += 2) {
			int runLength = runs[k];
			int value = runs[k + 1];

			if (runLength == 1) {
				outStr.append("0"); // 单个值标志
				outStr.append("00"); // 使用rleSizes[0]
				outStr.append(String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0'));
			} else {
				outStr.append("1"); // 连续值标志
				// 选择合适的rleSize索引
				int idx = getRLESizeIndex(runLength, rleSizes);
				outStr.append(String.format("%2s", Integer.toBinaryString(idx)).replace(' ', '0'));
				outStr.append(String.format("%" + rleSizes[idx] + "s",
						Integer.toBinaryString(runLength - 1)).replace(' ', '0'));
				outStr.append(String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0'));
			}
		}

		// 合并头部和编码数据
		String fullBits = header.toString() + outStr.toString();
		// 填充到字节边界
		int padding = 8 - (fullBits.length() % 8);
		if (padding > 0) fullBits += "0".repeat(padding);

		// 转换为字节数组
		for (int j = 0; j < fullBits.length(); j += 8) {
			baos.write((byte) Integer.parseInt(fullBits.substring(j, j + 8), 2));
		}

		return baos.toByteArray();
	}

	// 基础RLE编码实现
	private static int[] baseRLEncode(byte[] data) {
		List<Integer> runs = new ArrayList<>();
		int currentVal = data[0] & 0xFF;
		int count = 1;

		for (int i = 1; i < data.length; i++) {
			int val = data[i] & 0xFF;
			if (val == currentVal) {
				count++;
			} else {
				runs.add(count);
				runs.add(currentVal);
				currentVal = val;
				count = 1;
			}
		}
		runs.add(count);
		runs.add(currentVal);

		// 转换为int数组
		int[] result = new int[runs.size()];
		for (int i = 0; i < runs.size(); i++) {
			result[i] = runs.get(i);
		}
		return result;
	}

	// 获取RLE尺寸索引
	private static int getRLESizeIndex(int runLength, int[] rleSizes) {
		for (int i = 0; i < rleSizes.length; i++) {
			if (runLength <= (1 << rleSizes[i])) {
				return i;
			}
		}
		return rleSizes.length - 1;
	}

	// 图像转换方法
	public static byte[] imageToRLE(String imagePath) throws IOException {
		BufferedImage image = ImageIO.read(new File(imagePath));
		int width = image.getWidth();
		int height = image.getHeight();

		byte[] mask = new byte[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = image.getRGB(x, y);
				mask[y * width + x] = (byte) ((rgb >> 24) & 0xFF);
			}
		}

		// 转换为4通道并重复
		byte[] fourChannel = new byte[mask.length * 4];
		for (int i = 0; i < mask.length; i++) {
			fourChannel[i * 4] = mask[i];
			fourChannel[i * 4 + 1] = mask[i];
			fourChannel[i * 4 + 2] = mask[i];
			fourChannel[i * 4 + 3] = mask[i];
		}

		int[] rleSizes = {3, 4, 8, 16};
		return encodeRLE(fourChannel, 8, rleSizes);
	}

	// 保存图像方法
	public static void saveImage(byte[] data, int width, int height, String path) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		byte[] singleChannel = new byte[width * height];

		// 提取alpha通道
		for (int i = 0; i < data.length; i += 4) {
			singleChannel[i / 4] = data[i + 3];   // 保留alpha通道
		}

		image.getRaster().setDataElements(0, 0, width, height, singleChannel);
		ImageIO.write(image, "PNG", new File(path));
	}

	public static void saveImageWithOverlay(int[] data, int width, int height, String originalPath, String outputPath, int overlayColor) {
		saveImageWithOverlay(decodeRLE(intToBinary(data)), width, height, originalPath, outputPath, overlayColor);
	}

	public static void saveImageWithOverlay(byte[] data, int width, int height, String originalImagePath, String outputPath, int overlayColor) {
		BufferedImage originalImage = null;
		try {
			originalImage = ImageIO.read(new File(originalImagePath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 确保原图尺寸与标签一致
		if (originalImage.getWidth() != width || originalImage.getHeight() != height) {
			throw new IllegalArgumentException("Original image dimensions do not match the decoded RLE dimensions.");
		}

		// 解码后的标签图像
		byte[] singleChannel = new byte[width * height];
		for (int i = 0; i < data.length; i += 4) {
			singleChannel[i / 4] = (byte) (data[i + 3] & 0xFF); // 提取alpha通道
		}

		// 创建标签图像
		BufferedImage labelImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int value = singleChannel[y * width + x] & 0xFF;
				if (value > 0) { // 如果标签值非0，则设置颜色
					labelImage.setRGB(x, y, overlayColor); // 半透明红色 (ARGB: Alpha=128, Red=255)
				} else {
					labelImage.setRGB(x, y, 0x00000000); // 完全透明
				}
			}
		}

		// 创建叠加图像
		BufferedImage overlayedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int originalPixel = originalImage.getRGB(x, y);
				int labelPixel = labelImage.getRGB(x, y);

				// 混合像素值（根据透明度）
				int blendedPixel = blendPixels(originalPixel, labelPixel);
				overlayedImage.setRGB(x, y, blendedPixel);
			}
		}

		// 保存叠加后的图像
		try {
			ImageIO.write(overlayedImage, "PNG", new File(outputPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveImageWithMultiOverlayInt(Map<Long, int[]> rleDataMapByte, int width, int height,
													String originalImagePath, String outputPath,
													Map<Long, String> labelColors) {
		Map<Long,byte[]> rleDataMap = new HashMap<>();
		rleDataMapByte.forEach((key, value) -> {
			rleDataMap.put(key, decodeRLE(intToBinary(value)));
		});

		saveImageWithMultiOverlay(rleDataMap, width, height, originalImagePath,outputPath, labelColors);
	}

	// 保存图像并叠加多个标签到原图
	public static void saveImageWithMultiOverlay(
			Map<Long, byte[]> rleDataMap,
			int width,
			int height,
			String originalImagePath,
			String outputPath,
			Map<Long, String> labelColors) {

		// 加载原图
		BufferedImage originalImage = null;
		try {
			originalImage = ImageIO.read(new File(originalImagePath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// 确保原图尺寸与标签一致
		if (originalImage.getWidth() != width || originalImage.getHeight() != height) {
			throw new IllegalArgumentException("Original image dimensions do not match the decoded RLE dimensions.");
		}

		// 创建透明画布用于叠加
		BufferedImage overlayedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				overlayedImage.setRGB(x, y, originalImage.getRGB(x, y)); // 初始化为原图
			}
		}

		// 遍历每个标签
		for (Map.Entry<Long, byte[]> entry : rleDataMap.entrySet()) {
			long tagId = entry.getKey();
			byte[] rleData = entry.getValue();

			// 解码后的单通道数据
			byte[] singleChannel = new byte[width * height];
			for (int i = 0; i < rleData.length; i += 4) {
				singleChannel[i / 4] = (byte) (rleData[i + 3] & 0xFF); // 提取alpha通道
			}

			// 获取当前标签的颜色
			String hexColor = labelColors.getOrDefault(tagId, "80FF0000"); // 默认半透明红色
			int overlayColor = parseHexColor(hexColor);

			// 将标签叠加到图像上
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int value = singleChannel[y * width + x] & 0xFF;
					if (value > 0) { // 如果标签值非0，则设置颜色
						int blendedPixel = blendPixels(overlayedImage.getRGB(x, y), overlayColor);
						overlayedImage.setRGB(x, y, blendedPixel);
					}
				}
			}
		}

		// 保存叠加后的图像
		try {
			ImageIO.write(overlayedImage, "PNG", new File(outputPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// 解析十六进制颜色字符串为ARGB整数
	private static int parseHexColor(String hexColor) {
		if (hexColor.startsWith("#")) {
			hexColor = hexColor.substring(1);
		}

		// 处理 #RRGGBB 和 #AARRGGBB 两种格式
		if (hexColor.length() == 6) {
			int r = Integer.parseInt(hexColor.substring(0, 2), 16);
			int g = Integer.parseInt(hexColor.substring(2, 4), 16);
			int b = Integer.parseInt(hexColor.substring(4, 6), 16);
			return new Color(r, g, b).getRGB(); // 默认 Alpha=255
		} else if (hexColor.length() == 8) {
			// 包含透明度
			int alpha = Integer.parseInt(hexColor.substring(0, 2), 16);
			int red = Integer.parseInt(hexColor.substring(2, 4), 16);
			int green = Integer.parseInt(hexColor.substring(4, 6), 16);
			int blue = Integer.parseInt(hexColor.substring(6, 8), 16);
			return (alpha << 24) | (red << 16) | (green << 8) | blue;
		} else {
			throw new IllegalArgumentException("Invalid hex color format: " + hexColor);
		}
	}

	// 像素混合方法
	private static int blendPixels(int backgroundPixel, int overlayPixel) {
		// 提取背景像素的ARGB值
		int bgAlpha = (backgroundPixel >> 24) & 0xFF;
		int bgRed = (backgroundPixel >> 16) & 0xFF;
		int bgGreen = (backgroundPixel >> 8) & 0xFF;
		int bgBlue = backgroundPixel & 0xFF;

		// 提取叠加像素的ARGB值
		int overlayAlpha = (overlayPixel >> 24) & 0xFF;
		int overlayRed = (overlayPixel >> 16) & 0xFF;
		int overlayGreen = (overlayPixel >> 8) & 0xFF;
		int overlayBlue = overlayPixel & 0xFF;

		// 计算混合后的颜色
		int alpha = overlayAlpha + ((255 - overlayAlpha) * bgAlpha) / 255;
		int red = (overlayRed * overlayAlpha + bgRed * bgAlpha * (255 - overlayAlpha) / 255) / 255;
		int green = (overlayGreen * overlayAlpha + bgGreen * bgAlpha * (255 - overlayAlpha) / 255) / 255;
		int blue = (overlayBlue * overlayAlpha + bgBlue * bgAlpha * (255 - overlayAlpha) / 255) / 255;
		// 合并为ARGB格式
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	// 生成标注数据
	public static Map<String, Object> imageToAnnotation(String imagePath, String labelName,
														String fromName, String toName, boolean isGroundTruth) throws IOException {
		BufferedImage image = ImageIO.read(new File(imagePath));
		int width = image.getWidth();
		int height = image.getHeight();

		byte[] mask = new byte[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = image.getRGB(x, y);
				mask[y * width + x] = (byte) ((rgb & 0xFF) > 128 ? 255 : 0);
			}
		}

		byte[] fourChannel = new byte[mask.length * 4];
		for (int i = 0; i < mask.length; i++) {
			fourChannel[i * 4] = mask[i];
			fourChannel[i * 4 + 1] = mask[i];
			fourChannel[i * 4 + 2] = mask[i];
			fourChannel[i * 4 + 3] = mask[i];
		}

		int[] rleSizes = {3, 4, 8, 16};
		byte[] rleData = encodeRLE(fourChannel, 8, rleSizes);

		Map<String, Object> result = new HashMap<>();
		result.put("rle", rleData);
		result.put("original_width", width);
		result.put("original_height", height);
		result.put("brushlabels", new String[]{labelName});
		result.put("from_name", fromName);
		result.put("to_name", toName);
		result.put("ground_truth", isGroundTruth);

		return result;
	}

	public static void main(String[] args) {
//		try {
//			// 示例用法
//			byte[] rleData = imageToRLE("input.png");
//			saveImage(decodeRLE(rleData), 640, 480, "output.png");
//
//			// 生成标注示例
//			Map<String, Object> annotation = imageToAnnotation(
//					"mask.png", "object", "brush", "image", true);
//			System.out.println("Generated annotation: " + annotation);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		int[] data = {0, 1, 0, 0, 57, 27, 255, 48, 187, 0, 140, 66, 51, 232, 217, 163, 153, 142, 255, 27, 224, 71, 113, 28, 12, 104, 145, 134, 240, 25, 112, 8, 206, 35, 188, 143, 211, 45, 255, 199, 149, 28, 84, 105, 81, 134, 112, 24, 48, 8, 194, 99, 235, 206, 127, 241, 212, 198, 101, 192, 94, 192, 35, 52, 207, 255, 241, 223, 70, 219, 24, 119, 1, 111, 0, 141, 27, 67, 255, 199, 209, 26, 135, 1, 87, 0, 140, 94, 55, 248, 238, 35, 174, 141, 158, 50, 109, 31, 255, 29, 116, 108, 49, 139, 112, 20, 112, 8, 202, 35, 235, 191, 255, 28, 244, 116, 215, 255, 227, 206, 142, 190, 57, 92, 103, 1, 28, 52, 116, 241, 234, 99, 127, 248, 245, 163, 17, 224, 39, 224, 17, 144, 71, 207, 133, 255, 227, 0, 141, 210, 59, 72, 232, 35, 66, 201, 96, 17, 138, 198, 171, 28, 172, 122, 216, 175, 254, 60, 8, 197, 56, 9, 184, 4, 97, 49, 235, 97, 191, 248, 195, 115, 88, 4, 102, 49, 215, 98, 127, 248, 248, 227, 19, 224, 38, 96, 17, 198, 225, 191, 248, 246, 179, 184, 4, 100, 17, 202, 226, 63, 248, 236, 163, 16, 224, 37, 96, 17, 152, 225, 191, 248, 253, 227, 70, 207, 224, 17, 155, 199, 179, 135, 255, 227, 181, 224, 37, 96, 17, 214, 97, 127, 248, 238, 227, 35, 209, 96, 17, 184, 71, 233, 134, 255, 227, 133, 224, 36, 96, 17, 152, 97, 127, 248, 242, 35, 17, 210, 96, 17, 184, 225, 255, 248, 223, 56, 8, 248, 4, 119, 120, 95, 254, 51, 157, 46, 1, 24, 68, 121, 88, 127, 254, 56, 142, 2, 46, 1, 24, 118, 23, 255, 143, 7, 81, 128, 70, 165, 135, 255, 227, 46, 224, 34, 96, 17, 157, 71, 239, 132, 255, 227, 56, 212, 224, 17, 164, 97, 191, 248, 242, 227, 23, 224, 33, 224, 17, 213, 225, 63, 248, 245, 181, 120, 4, 104, 88, 111, 254, 62, 248, 199, 248, 8, 56, 4, 99, 24, 95, 254, 56, 253, 102, 1, 26, 222, 27, 255, 143, 35, 128, 131, 128, 70, 159, 133, 255, 227, 48, 214, 224, 17, 215, 97, 191, 248, 226, 184, 8, 24, 4, 111, 56, 79, 254, 61, 61, 118, 1, 25, 4, 124, 216, 111, 254, 50, 77, 254, 1, 29, 86, 19, 255, 142, 147, 95, 128, 70, 37, 29, 230, 23, 255, 141, 151, 127, 128, 71, 231, 132, 255, 227, 121, 216, 96, 17, 152, 71, 225, 132, 255, 227, 235, 223, 96, 17, 145, 97, 127, 248, 206, 246, 56, 4, 108, 120, 95, 254, 57, 237, 238, 1, 25, 38, 23, 255, 140, 55, 99, 128, 70, 25, 31, 78, 23, 255, 141, 91, 121, 128, 70, 73, 133, 255, 227, 16, 217, 96, 17, 182, 225, 127, 248, 247, 247, 152, 4, 100, 152, 95, 254, 49, 29, 150, 1, 24, 68, 123, 56, 95, 254, 49, 141, 222, 1, 25, 38, 23, 255, 140, 67, 103, 128, 70, 121, 133, 255, 227, 32, 221, 224, 17, 146, 97, 127, 248, 196, 246, 120, 4, 98, 152, 95, 254, 50, 61, 222, 1, 25, 38, 23, 255, 140, 71, 103, 128, 70, 41, 133, 255, 227, 33, 221, 224, 17, 144, 225, 127, 248, 213, 246, 120, 4, 98, 152, 95, 254, 50, 13, 230, 1, 31, 150, 19, 255, 143, 46, 48, 237, 150, 1, 24, 166, 23, 255, 140, 131, 121, 128, 71, 77, 133, 255, 227, 119, 217, 96, 17, 138, 97, 127, 248, 200, 55, 152, 4, 102, 24, 95, 254, 58, 253, 150, 1, 24, 166, 23, 255, 140, 131, 123, 128, 71, 93, 132, 255, 227, 229, 140, 207, 99, 128, 70, 41, 133, 255, 227, 32, 222, 224, 17, 167, 225, 127, 248, 241, 246, 56, 4, 98, 152, 95, 254, 50, 13, 246, 1, 31, 6, 23, 255, 141, 63, 97, 128, 70, 41, 133, 255, 227, 33, 223, 96, 17, 159, 225, 127, 248, 252, 163, 27, 215, 224, 17, 143, 225, 127, 248, 200, 55, 248, 4, 116, 120, 95, 254, 59, 120, 206, 181, 216, 4, 118, 88, 95, 254, 49, 29, 254, 1, 25, 126, 27, 255, 143, 202, 50, 205, 102, 1, 27, 6, 23, 255, 143, 43, 128, 131, 128, 71, 25, 135, 255, 227, 187, 140, 119, 83, 128, 70, 201, 134, 255, 227, 152, 224, 32, 224, 17, 132, 71, 185, 136, 255, 227, 136, 140, 163, 75, 128, 70, 117, 30, 102, 31, 255, 141, 63, 128, 133, 128, 70, 111, 31, 182, 39, 255, 142, 126, 52, 45, 14, 1, 24, 236, 116, 248, 143, 254, 53, 206, 2, 38, 1, 25, 132, 123, 120, 175, 254, 61, 72, 223, 163, 67, 206, 96, 17, 133, 71, 5, 137, 255, 227, 128, 224, 35, 96, 17, 141, 199, 193, 140, 255, 227, 212, 159, 2, 58, 232, 214, 35, 8, 201, 224, 17, 142, 199, 5, 29, 252, 124, 248, 159, 254, 55, 110, 2, 70, 1, 24, 204, 111, 241, 228, 99, 255, 248, 253, 35, 134, 142, 2, 51, 216, 195, 49, 184, 4, 102, 145, 204, 226, 255, 248, 249, 163, 80, 224, 38, 96, 17, 170, 199, 237, 173, 255, 227, 130, 140, 43, 128, 159, 128, 70, 173, 30, 22, 167, 255, 142, 158, 49, 222, 2, 158, 1, 25, 244, 118, 250, 95, 254, 60, 200, 206, 184, 10, 248, 4, 98, 49, 176, 199, 43, 31, 62, 127, 255, 143, 158, 53, 254, 2, 238, 1, 24, 76, 102, 145, 172, 199, 51, 152, 255, 227, 204, 142, 2, 52, 174, 3, 30, 1, 24, 220, 104, 209, 186, 199, 89, 30, 254, 67, 255, 143, 198, 57, 136, 210, 248, 13, 120, 4, 97, 216, 237, 2, 51, 126, 14, 158, 1, 27, 52, 116, 249, 47, 2, 57, 88, 215, 99, 50, 140, 35, 128, 201, 128, 70, 105, 29, 30, 99, 255, 143, 122, 59, 168, 221, 99, 31, 224, 47, 96, 17, 162, 199, 131, 158, 255, 227, 203, 141, 222, 50, 30, 2, 214, 1, 24, 172, 126, 186, 47, 254, 60, 232, 222, 163, 35, 224, 43, 224, 17, 222, 233, 191, 248, 243, 35, 101, 224, 42, 96, 17, 140, 106, 127, 248, 236, 35, 21, 224, 41, 96, 17, 148, 97, 255, 248, 239, 49, 252, 4, 112, 113, 212, 199, 163, 142, 255, 227, 240, 141, 230, 48, 142, 2, 142, 1, 31, 150, 19, 255, 142, 106, 51, 236, 166, 1, 25, 84, 111, 145, 229, 99, 63, 248, 246, 163, 65, 224, 40, 96, 17, 174, 223, 255, 141, 199, 51, 128, 70, 43, 27, 36, 118, 145, 251, 226, 191, 248, 220, 56, 8, 248, 4, 98, 81, 155, 198, 19, 133, 192, 35, 44, 141, 198, 55, 88, 203, 243, 216, 4, 105, 145, 205, 199, 211, 136, 255, 227, 58, 224, 34, 96, 17, 134, 199, 121, 111, 254, 56, 157, 78, 1, 24, 188, 117, 88, 127, 254, 61, 110, 2, 38, 1, 27, 246, 19, 255, 140, 207, 85, 128, 70, 187, 31, 142, 27, 255, 141, 95, 128, 135, 128, 71, 89, 132, 255, 227, 100, 213, 224, 17, 143, 71, 91, 133, 255, 227, 237, 140, 135, 128, 133, 128, 71, 197, 132, 255, 227, 119, 214, 224, 17, 205, 225, 127, 248, 245, 99, 9, 224, 32, 96, 17, 144, 97, 127, 248, 214, 117, 184, 4, 99, 184, 111, 254, 54, 46, 2, 6, 1, 26, 86, 23, 255, 140, 87, 93, 128, 70, 223, 133, 255, 227, 171, 224, 32, 96, 17, 193, 225, 127, 253, 134, 1, 29, 150, 19, 255, 143, 46, 48, 141, 254, 1, 28, 134, 19, 255, 143, 11, 97, 128, 70, 109, 133, 255, 227, 96, 223, 224, 17, 200, 97, 63, 248, 233, 54, 56, 4, 124, 120, 79, 254, 59, 109, 254, 1, 28, 134, 19, 255, 142, 147, 99, 128, 70, 249, 132, 255, 227, 245, 223, 224, 17, 200, 97, 63, 248, 233, 54, 56, 4, 101, 88, 95, 254, 51, 29, 246, 1, 28, 134, 19, 255, 142, 147, 101, 128, 71, 217, 132, 255, 227, 86, 223, 96, 17, 200, 97, 63, 248, 253, 99, 42, 216, 224, 17, 228, 97, 63, 248, 223, 55, 216, 4, 111, 248, 95, 254, 58, 205, 142, 1, 29, 46, 19, 255, 142, 119, 125, 128, 70, 67, 133, 255, 227, 244, 216, 224, 17, 200, 97, 63, 248, 233, 55, 248, 4, 113, 152, 95, 254, 58, 72, 194, 117, 248, 4, 114, 24, 79, 254, 58, 78, 2, 6, 1, 31, 134, 23, 255, 143, 27, 95, 128, 71, 33, 132, 255, 227, 164, 224, 32, 96, 17, 202, 225, 191, 248, 235, 35, 48, 214, 224, 17, 200, 225, 63, 248, 233, 56, 8, 24, 4, 97, 177, 241, 225, 255, 248, 231, 227, 24, 213, 224, 17, 213, 225, 63, 248, 232, 248, 8, 56, 4, 103, 152, 143, 254, 63, 88, 223, 35, 8, 212, 224, 17, 247, 97, 63, 248, 225, 184, 8, 88, 4, 105, 113, 241, 98, 63, 248, 251, 227, 116, 140, 39, 79, 128, 97, 127, 248, 208, 56, 8, 120, 4, 104, 120, 191, 254, 59, 72, 215, 35, 12, 209, 224, 17, 132, 97, 127, 248, 206, 56, 8, 152, 4, 102, 17, 226, 227, 63, 248, 238, 99, 108, 140, 119, 65, 128, 71, 25, 133, 255, 227, 54, 224, 35, 96, 17, 172, 71, 193, 142, 255, 227, 215, 142, 222, 57, 152, 223, 35, 70, 140, 43, 49, 128, 70, 159, 134, 255, 227, 18, 224, 36, 96, 17, 188, 199, 235, 146, 255, 227, 218, 142, 190, 55, 216, 211, 50, 90, 4, 104, 145, 218, 97, 191, 248, 233, 120, 9, 88, 4, 98, 241, 206, 236, 63, 248, 249, 99, 14, 224, 38, 96, 17, 151, 71, 47, 31, 126, 183, 255, 141, 51, 128, 161, 128, 70, 101, 28, 76, 124, 58, 159, 254, 57, 142, 2, 166, 1, 25, 164, 112, 81, 232, 105, 127, 248, 224, 248, 11, 24, 4, 99, 209, 161, 70, 191, 27, 228, 115, 145, 221, 71, 147, 156, 255, 227, 217, 140, 247, 128, 195, 128, 70, 103, 27, 4, 113, 145, 225, 101, 127, 248, 238, 163, 98, 226, 25, 224, 0};

		Map<Long, int[]> rleDataMap = new HashMap<>();

		rleDataMap.put(98L, new int[]{0, 1, 0, 0, 57, 27, 255, 48, 187, 0, 140, 66, 51, 232, 217, 163, 153, 142, 255, 27, 224, 71, 113, 28, 12, 104, 145, 134, 240, 25, 112, 8, 206, 35, 188, 143, 211, 45, 255, 199, 149, 28, 84, 105, 81, 134, 112, 24, 48, 8, 194, 99, 235, 206, 127, 241, 212, 198, 101, 192, 94, 192, 35, 52, 207, 255, 241, 223, 70, 219, 24, 119, 1, 111, 0, 141, 27, 67, 255, 199, 209, 26, 135, 1, 87, 0, 140, 94, 55, 248, 238, 35, 174, 141, 158, 50, 109, 31, 255, 29, 116, 108, 49, 139, 112, 20, 112, 8, 202, 35, 235, 191, 255, 28, 244, 116, 215, 255, 227, 206, 142, 190, 57, 92, 103, 1, 28, 52, 116, 241, 234, 99, 127, 248, 245, 163, 17, 224, 39, 224, 17, 144, 71, 207, 133, 255, 227, 0, 141, 210, 59, 72, 232, 35, 66, 201, 96, 17, 138, 198, 171, 28, 172, 122, 216, 175, 254, 60, 8, 197, 56, 9, 184, 4, 97, 49, 235, 97, 191, 248, 195, 115, 88, 4, 102, 49, 215, 98, 127, 248, 248, 227, 19, 224, 38, 96, 17, 198, 225, 191, 248, 246, 179, 184, 4, 100, 17, 202, 226, 63, 248, 236, 163, 16, 224, 37, 96, 17, 152, 225, 191, 248, 253, 227, 70, 207, 224, 17, 155, 199, 179, 135, 255, 227, 181, 224, 37, 96, 17, 214, 97, 127, 248, 238, 227, 35, 209, 96, 17, 184, 71, 233, 134, 255, 227, 133, 224, 36, 96, 17, 152, 97, 127, 248, 242, 35, 17, 210, 96, 17, 184, 225, 255, 248, 223, 56, 8, 248, 4, 119, 120, 95, 254, 51, 157, 46, 1, 24, 68, 121, 88, 127, 254, 56, 142, 2, 46, 1, 24, 118, 23, 255, 143, 7, 81, 128, 70, 165, 135, 255, 227, 46, 224, 34, 96, 17, 157, 71, 239, 132, 255, 227, 56, 212, 224, 17, 164, 97, 191, 248, 242, 227, 23, 224, 33, 224, 17, 213, 225, 63, 248, 245, 181, 120, 4, 104, 88, 111, 254, 62, 248, 199, 248, 8, 56, 4, 99, 24, 95, 254, 56, 253, 102, 1, 26, 222, 27, 255, 143, 35, 128, 131, 128, 70, 159, 133, 255, 227, 48, 214, 224, 17, 215, 97, 191, 248, 226, 184, 8, 24, 4, 111, 56, 79, 254, 61, 61, 118, 1, 25, 4, 124, 216, 111, 254, 50, 77, 254, 1, 29, 86, 19, 255, 142, 147, 95, 128, 70, 37, 29, 230, 23, 255, 141, 151, 127, 128, 71, 231, 132, 255, 227, 121, 216, 96, 17, 152, 71, 225, 132, 255, 227, 235, 223, 96, 17, 145, 97, 127, 248, 206, 246, 56, 4, 108, 120, 95, 254, 57, 237, 238, 1, 25, 38, 23, 255, 140, 55, 99, 128, 70, 25, 31, 78, 23, 255, 141, 91, 121, 128, 70, 73, 133, 255, 227, 16, 217, 96, 17, 182, 225, 127, 248, 247, 247, 152, 4, 100, 152, 95, 254, 49, 29, 150, 1, 24, 68, 123, 56, 95, 254, 49, 141, 222, 1, 25, 38, 23, 255, 140, 67, 103, 128, 70, 121, 133, 255, 227, 32, 221, 224, 17, 146, 97, 127, 248, 196, 246, 120, 4, 98, 152, 95, 254, 50, 61, 222, 1, 25, 38, 23, 255, 140, 71, 103, 128, 70, 41, 133, 255, 227, 33, 221, 224, 17, 144, 225, 127, 248, 213, 246, 120, 4, 98, 152, 95, 254, 50, 13, 230, 1, 31, 150, 19, 255, 143, 46, 48, 237, 150, 1, 24, 166, 23, 255, 140, 131, 121, 128, 71, 77, 133, 255, 227, 119, 217, 96, 17, 138, 97, 127, 248, 200, 55, 152, 4, 102, 24, 95, 254, 58, 253, 150, 1, 24, 166, 23, 255, 140, 131, 123, 128, 71, 93, 132, 255, 227, 229, 140, 207, 99, 128, 70, 41, 133, 255, 227, 32, 222, 224, 17, 167, 225, 127, 248, 241, 246, 56, 4, 98, 152, 95, 254, 50, 13, 246, 1, 31, 6, 23, 255, 141, 63, 97, 128, 70, 41, 133, 255, 227, 33, 223, 96, 17, 159, 225, 127, 248, 252, 163, 27, 215, 224, 17, 143, 225, 127, 248, 200, 55, 248, 4, 116, 120, 95, 254, 59, 120, 206, 181, 216, 4, 118, 88, 95, 254, 49, 29, 254, 1, 25, 126, 27, 255, 143, 202, 50, 205, 102, 1, 27, 6, 23, 255, 143, 43, 128, 131, 128, 71, 25, 135, 255, 227, 187, 140, 119, 83, 128, 70, 201, 134, 255, 227, 152, 224, 32, 224, 17, 132, 71, 185, 136, 255, 227, 136, 140, 163, 75, 128, 70, 117, 30, 102, 31, 255, 141, 63, 128, 133, 128, 70, 111, 31, 182, 39, 255, 142, 126, 52, 45, 14, 1, 24, 236, 116, 248, 143, 254, 53, 206, 2, 38, 1, 25, 132, 123, 120, 175, 254, 61, 72, 223, 163, 67, 206, 96, 17, 133, 71, 5, 137, 255, 227, 128, 224, 35, 96, 17, 141, 199, 193, 140, 255, 227, 212, 159, 2, 58, 232, 214, 35, 8, 201, 224, 17, 142, 199, 5, 29, 252, 124, 248, 159, 254, 55, 110, 2, 70, 1, 24, 204, 111, 241, 228, 99, 255, 248, 253, 35, 134, 142, 2, 51, 216, 195, 49, 184, 4, 102, 145, 204, 226, 255, 248, 249, 163, 80, 224, 38, 96, 17, 170, 199, 237, 173, 255, 227, 130, 140, 43, 128, 159, 128, 70, 173, 30, 22, 167, 255, 142, 158, 49, 222, 2, 158, 1, 25, 244, 118, 250, 95, 254, 60, 200, 206, 184, 10, 248, 4, 98, 49, 176, 199, 43, 31, 62, 127, 255, 143, 158, 53, 254, 2, 238, 1, 24, 76, 102, 145, 172, 199, 51, 152, 255, 227, 204, 142, 2, 52, 174, 3, 30, 1, 24, 220, 104, 209, 186, 199, 89, 30, 254, 67, 255, 143, 198, 57, 136, 210, 248, 13, 120, 4, 97, 216, 237, 2, 51, 126, 14, 158, 1, 27, 52, 116, 249, 47, 2, 57, 88, 215, 99, 50, 140, 35, 128, 201, 128, 70, 105, 29, 30, 99, 255, 143, 122, 59, 168, 221, 99, 31, 224, 47, 96, 17, 162, 199, 131, 158, 255, 227, 203, 141, 222, 50, 30, 2, 214, 1, 24, 172, 126, 186, 47, 254, 60, 232, 222, 163, 35, 224, 43, 224, 17, 222, 233, 191, 248, 243, 35, 101, 224, 42, 96, 17, 140, 106, 127, 248, 236, 35, 21, 224, 41, 96, 17, 148, 97, 255, 248, 239, 49, 252, 4, 112, 113, 212, 199, 163, 142, 255, 227, 240, 141, 230, 48, 142, 2, 142, 1, 31, 150, 19, 255, 142, 106, 51, 236, 166, 1, 25, 84, 111, 145, 229, 99, 63, 248, 246, 163, 65, 224, 40, 96, 17, 174, 223, 255, 141, 199, 51, 128, 70, 43, 27, 36, 118, 145, 251, 226, 191, 248, 220, 56, 8, 248, 4, 98, 81, 155, 198, 19, 133, 192, 35, 44, 141, 198, 55, 88, 203, 243, 216, 4, 105, 145, 205, 199, 211, 136, 255, 227, 58, 224, 34, 96, 17, 134, 199, 121, 111, 254, 56, 157, 78, 1, 24, 188, 117, 88, 127, 254, 61, 110, 2, 38, 1, 27, 246, 19, 255, 140, 207, 85, 128, 70, 187, 31, 142, 27, 255, 141, 95, 128, 135, 128, 71, 89, 132, 255, 227, 100, 213, 224, 17, 143, 71, 91, 133, 255, 227, 237, 140, 135, 128, 133, 128, 71, 197, 132, 255, 227, 119, 214, 224, 17, 205, 225, 127, 248, 245, 99, 9, 224, 32, 96, 17, 144, 97, 127, 248, 214, 117, 184, 4, 99, 184, 111, 254, 54, 46, 2, 6, 1, 26, 86, 23, 255, 140, 87, 93, 128, 70, 223, 133, 255, 227, 171, 224, 32, 96, 17, 193, 225, 127, 253, 134, 1, 29, 150, 19, 255, 143, 46, 48, 141, 254, 1, 28, 134, 19, 255, 143, 11, 97, 128, 70, 109, 133, 255, 227, 96, 223, 224, 17, 200, 97, 63, 248, 233, 54, 56, 4, 124, 120, 79, 254, 59, 109, 254, 1, 28, 134, 19, 255, 142, 147, 99, 128, 70, 249, 132, 255, 227, 245, 223, 224, 17, 200, 97, 63, 248, 233, 54, 56, 4, 101, 88, 95, 254, 51, 29, 246, 1, 28, 134, 19, 255, 142, 147, 101, 128, 71, 217, 132, 255, 227, 86, 223, 96, 17, 200, 97, 63, 248, 253, 99, 42, 216, 224, 17, 228, 97, 63, 248, 223, 55, 216, 4, 111, 248, 95, 254, 58, 205, 142, 1, 29, 46, 19, 255, 142, 119, 125, 128, 70, 67, 133, 255, 227, 244, 216, 224, 17, 200, 97, 63, 248, 233, 55, 248, 4, 113, 152, 95, 254, 58, 72, 194, 117, 248, 4, 114, 24, 79, 254, 58, 78, 2, 6, 1, 31, 134, 23, 255, 143, 27, 95, 128, 71, 33, 132, 255, 227, 164, 224, 32, 96, 17, 202, 225, 191, 248, 235, 35, 48, 214, 224, 17, 200, 225, 63, 248, 233, 56, 8, 24, 4, 97, 177, 241, 225, 255, 248, 231, 227, 24, 213, 224, 17, 213, 225, 63, 248, 232, 248, 8, 56, 4, 103, 152, 143, 254, 63, 88, 223, 35, 8, 212, 224, 17, 247, 97, 63, 248, 225, 184, 8, 88, 4, 105, 113, 241, 98, 63, 248, 251, 227, 116, 140, 39, 79, 128, 97, 127, 248, 208, 56, 8, 120, 4, 104, 120, 191, 254, 59, 72, 215, 35, 12, 209, 224, 17, 132, 97, 127, 248, 206, 56, 8, 152, 4, 102, 17, 226, 227, 63, 248, 238, 99, 108, 140, 119, 65, 128, 71, 25, 133, 255, 227, 54, 224, 35, 96, 17, 172, 71, 193, 142, 255, 227, 215, 142, 222, 57, 152, 223, 35, 70, 140, 43, 49, 128, 70, 159, 134, 255, 227, 18, 224, 36, 96, 17, 188, 199, 235, 146, 255, 227, 218, 142, 190, 55, 216, 211, 50, 90, 4, 104, 145, 218, 97, 191, 248, 233, 120, 9, 88, 4, 98, 241, 206, 236, 63, 248, 249, 99, 14, 224, 38, 96, 17, 151, 71, 47, 31, 126, 183, 255, 141, 51, 128, 161, 128, 70, 101, 28, 76, 124, 58, 159, 254, 57, 142, 2, 166, 1, 25, 164, 112, 81, 232, 105, 127, 248, 224, 248, 11, 24, 4, 99, 209, 161, 70, 191, 27, 228, 115, 145, 221, 71, 147, 156, 255, 227, 217, 140, 247, 128, 195, 128, 70, 103, 27, 4, 113, 145, 225, 101, 127, 248, 238, 163, 98, 226, 25, 224, 0});
		rleDataMap.put(97L, new int[]{0,1,0,0,57,27,255,179,123,0,140,6,53,104,232,99,179,141,222,49,14,3,206,1,27,173,255,248,242,120,15,56,4,122,24,79,254,50,78,3,198,1,31,94,19,255,142,15,128,241,128,71,211,132,255,227,171,224,60,96,17,226,97,63,248,246,56,15,24,4,109,120,95,254,49,14,3,190,1,24,246,23,255,141,175,128,239,128,70,5,31,118,19,255,143,146,49,14,3,190,1,28,22,23,255,141,171,128,239,128,70,59,133,255,227,180,224,60,96,17,207,97,63,248,247,35,2,224,59,224,17,181,225,127,248,213,56,14,248,4,104,56,95,254,57,158,3,198,1,29,238,19,255,143,214,49,142,3,190,1,27,110,23,255,141,67,128,239,128,70,75,133,255,227,106,224,59,224,17,131,71,231,132,255,227,86,224,60,96,17,186,95,255,143,230,49,142,3,198,1,24,52,117,51,255,199,239,26,167,1,239,0,140,110,51,104,194,57,34,248,0});
		String originalPath = "H:\\ai平台导入测试\\class-test\\1218ly\\images\\img\\98_4_img.png";

		Map<Long,String> labelColors = new HashMap<>();
		labelColors.put(98L,"#FF0000");
		labelColors.put(97L,"#00FF00");
		if (!FileUtil.isDirectory("D:\\test")) {
			FileUtil.mkdir("D:\\test");
		}
		saveImageWithMultiOverlayInt(rleDataMap, 128, 128, originalPath, "D:\\test\\test98_4.png", labelColors);
	}
}
