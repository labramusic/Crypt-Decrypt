package hr.fer.zemris.java.tecaj.hw07.crypto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import hr.fer.zemris.java.tecaj.hw07.crypto.util.ByteConverter;

public class HexToByteTests {

	@Test
	public void test1() {
		byte[] bytes = ByteConverter.hexToByte("5a2217e3ee213ef1ffdee3a192e2ac7e");
		String hex = ByteConverter.byteToHex(bytes);
		assertEquals("5a2217e3ee213ef1ffdee3a192e2ac7e", hex);
	}

	@Test
	public void test2() {
		byte[] bytes = ByteConverter.hexToByte("000102030405060708090a0b0c0d0e0f");
		String hex = ByteConverter.byteToHex(bytes);
		assertEquals("000102030405060708090a0b0c0d0e0f", hex);
	}

	@Test
	public void hexToByte()
	{
		assertArrayEquals(new byte[]{0x00, 0x01, 0x02}, ByteConverter.hexToByte("000102"));
		assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD}, ByteConverter.hexToByte("FFFEFD"));
		assertArrayEquals(new byte[]{(byte) 0xFF}, ByteConverter.hexToByte("FF"));
		assertArrayEquals(new byte[]{(byte) 0x00}, ByteConverter.hexToByte("00"));
		assertArrayEquals(new byte[]{(byte) 0x01}, ByteConverter.hexToByte("01"));
		assertArrayEquals(new byte[]{(byte) 0x7F}, ByteConverter.hexToByte("7F"));
		assertArrayEquals(new byte[]{(byte) 0x80}, ByteConverter.hexToByte("80"));
	}

}
