package hr.fer.zemris.java.tecaj.hw07.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import hr.fer.zemris.java.tecaj.hw07.crypto.util.ByteConverter;

/**
 * Allows the user to encrypt/decrypt given file using the AES cryptoalgorithm
 * and the 128-bit encryption key or to calculate and check the SHA-256 file digest.
 * If SHA is checked, user can enter the expected digest and the result of comparing
 * the two digests is printed. While encrypting/decrypting, user is asked to input
 * a 16-byte key and 16-byte initialization vector.
 * @author labramusic
 *
 */
public class Crypto {

	/**
	 * Default streaming buffer size;
	 */
	private final static int BUFFER_SIZE = 4096;

	/**
	 * The main method.
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Invalid number of arguments.");
			System.exit(1);
		}
		String command = args[0];
		File file = new File(args[1]);
		if (!file.isFile()) {
			System.err.println("Given argument must be a file.");
			System.exit(1);
		}

		switch (command) {
		case "checksha":
			checkSha(file);
			break;

		case "encrypt":
			if (args.length != 3) {
				System.err.println("Invalid number of arguments.");
				System.exit(1);
			}
			encrypt(file, new File(args[2]), true);
			break;

		case "decrypt":
			if (args.length != 3) {
				System.err.println("Invalid number of arguments.");
				System.exit(1);
			}
			encrypt(file, new File(args[2]), false);
			break;

		default:
			System.err.println("Invalid command.");
			System.exit(1);
		}
	}

	/**
	 * Checks the SHA-256 file digest and compares it to the one given from the user.
	 * @param file the input file
	 */
	private static void checkSha(File file) {
		String filename = file.getName();
		System.out.println("Please provide expected sha-256 digest for "+filename+":");
		System.out.print("> ");
		Scanner sc = new Scanner(System.in);
		String expected = sc.nextLine();
		sc.close();

		String result = ByteConverter.byteToHex(digestFile(file));
		if (result.equals(expected)) {
			System.out.println("Digesting completed. Digest of "+filename+" matches expected digest.");
		} else {
			System.out.println("Digesting completed. Digest of "+filename+" does not match the expected digest.");
			System.out.println("Digest was: " + result);
		}
	}

	/**
	 * Digests the file using the SHA-256 message digest and returns its digest.
	 * @param file the file
	 * @return message digest
	 */
	private static byte[] digestFile(File file) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("SHA algorithm not found: "+e.getMessage());
			System.exit(1);
		}

		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				int r = is.read(buffer);
				if (r<1) break;
				md.update(buffer, 0, r);
			}
		} catch (IOException e) {
			System.err.println("Error handling I/O: "+e.getMessage());
			System.exit(1);
		}

		return md.digest();
	}

	/**
	 * Encrypts or decrypts the file and generates an output file.
	 * @param file the file to be encrypted/decrypted
	 * @param output the output file
	 * @param encrypt true if encrypting, false if decrypting
	 */
	private static void encrypt(File file, File output, boolean encrypt) {
		String filename = file.getName();
		String outputFilename = output.getName();
		Scanner sc = new Scanner(System.in);
		System.out.println("Please provide password as hex-encoded text (16 bytes, i.e. 32 hex-digits):");
		System.out.print("> ");
		String keyText = sc.nextLine();
		System.out.println("Please provide initialization vector as hex-encoded text (32 hex-digits):");
		System.out.print("> ");
		String ivText = sc.nextLine();
		sc.close();

		Cipher cipher = initialize(keyText, ivText, encrypt);
		byte[] ciphered = cipher(cipher, file);
		generate(ciphered, output);
		if (encrypt) {
			System.out.println("Encryption completed. Generated file "+outputFilename+" based on file "+filename+".");
		} else {
			System.out.println("Decryption completed. Generated file "+outputFilename+" based on file "+filename+".");
		}
	}

	/**
	 * Initializes the cipher object from the given parameters.
	 * @param keyText encryption/decryption key
	 * @param ivText initialization vector
	 * @param encrypt true if encrypting, false if decrypting
	 * @return cipher object
	 */
	private static Cipher initialize(String keyText, String ivText, boolean encrypt) {
		SecretKeySpec keySpec = new SecretKeySpec(ByteConverter.hexToByte(keyText), "AES");
		AlgorithmParameterSpec paramSpec = new IvParameterSpec(ByteConverter.hexToByte(ivText));
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("AES algorithm not found: "+e.getMessage());
			System.exit(1);
		} catch (NoSuchPaddingException e) {
			System.err.println("Padding not found: "+e.getMessage());
			System.exit(1);
		}

		try {
			cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, paramSpec);
		} catch (InvalidKeyException e) {
			System.err.println("Invalid key: "+e.getMessage());
			System.exit(1);
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("Invalid algorithm parameter: "+e.getMessage());
			System.exit(1);
		}
		return cipher;
	}

	/**
	 * Ciphers the file with the given cipher object and returns the array of ciphered bytes.
	 * @param cipher cipher object
	 * @param file input file
	 * @return array of ciphered bytes
	 */
	private static byte[] cipher(Cipher cipher, File file) {
		byte[] ciphered = null;
		try (InputStream is = new BufferedInputStream(new FileInputStream(file));
				ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[BUFFER_SIZE];
			int r;
			while ((r = is.read(buffer)) >= 1) {
				os.write(cipher.update(buffer, 0, r));
			}

			try {
				os.write(cipher.doFinal());
				ciphered = os.toByteArray();
			} catch (IllegalBlockSizeException e) {
				System.err.println("Illegal block size: "+e.getMessage());
				System.exit(1);
			} catch (BadPaddingException e) {
				System.err.println("Bad padding error: "+e.getMessage());
				System.exit(1);
			}

		} catch (IOException e) {
			System.err.println("Error handling I/O: "+e.getMessage());
			System.exit(1);
		}
		return ciphered;
	}

	/**
	 * Generates the encrypted/decrypted file from the given ciphered bytes.
	 * @param data ciphered bytes
	 * @param output output file
	 */
	private static void generate(byte[] data, File output) {
		try (InputStream is = new ByteArrayInputStream(data);
				OutputStream os = new BufferedOutputStream(new FileOutputStream(output))) {
			byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				int r = is.read(buffer);
				if (r<1) break;
				os.write(buffer, 0, r);
			}

		} catch (IOException e) {
			System.err.println("Error handling I/O: "+e.getMessage());
			System.exit(1);
		}
	}

}
