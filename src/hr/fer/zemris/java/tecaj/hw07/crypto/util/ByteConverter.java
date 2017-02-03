package hr.fer.zemris.java.tecaj.hw07.crypto.util;

import java.util.Formatter;

/**
 * Utility class for converting bytes to hex strings and vice versa.
 * @author labramusic
 *
 */
public class ByteConverter {

	/**
	 * Converts bytes to their hex values.
	 * @param bytes bytes to convert
	 * @return string of hex values
	 */
	public static String byteToHex(byte[] bytes) {
		Formatter formatter = new Formatter();
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	/**
	 * Converts hex values to their byte representations.
	 * @param text string of hex values
	 * @return resulting bytes
	 */
	public static byte[] hexToByte(String text) {
		int len = text.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(text.charAt(i), 16) << 4)
					+ Character.digit(text.charAt(i+1), 16));
		}
		return data;
	}

}
