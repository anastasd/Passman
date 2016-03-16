package dv.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.io.Console;

public class Passman {
	private static List<String[]> passwords = new ArrayList<String[]>();
	private static String IV = "xBWBRal9x0c6dN0A";
	private static String masterPass;
	
	/*
	 * @param String args[0] - set/get/del/getall/delall/list/look
	 * 
	 *
	 */
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("Available commands:");
			System.out.println("\tset <domain> <username> - adds a new password for <username> at <domain>. Overrides any existing");
			System.out.println("\tget <domain> <username> - gets the password for <username> at <domain>");
			System.out.println("\tgetall <domain> - gets all passwords at <domain>");
			System.out.println("\tdel <domain> <username> - deletes the password for <username> at <domain>");
			System.out.println("\tdelall <domain> - deletes all passwords at <domain>");
			System.out.println("\tlist - lists all domains and usernames stored in the file");
			System.out.println("\tlook <*domain*> - gets all passwords at <domain> by wildcard");
			
			System.exit(0);
		}
		/*
		 * @param String args[1] - domain
		 * @param String args[2] - username
		 */
		if (args[0].equals("get")) {
			if (args.length != 3) {
				System.out.println("Funcion \"get\" needs two arguments. Example usage: get domain username");
				System.exit(0);
			}
			__getPassword(args[1], args[2]);
		}
		
		/*
		 * @param String args[1] - domain
		 * @param String args[2] - username
		 */
		if (args[0].equals("set")) {
			if (args.length != 3) {
				System.out.println("Funcion \"set\" needs two arguments. Example usage: set domain username");
				System.exit(0);
			}
			__addPassword(args[1], args[2]);
		}
		
		/*
		 * @param String args[1] - domain
		 */
		if (args[0].equals("getall")) {
			if (args.length != 2) {
				System.out.println("Funcion \"getall\" needs one argument. Example usage: getall domain");
				System.exit(0);
			}
			__getAllPasswords(args[1]);
		}
		
		/*
		 * @param String args[1] - domain
		 * @param String args[2] - username
		 */
		if (args[0].equals("del")) {
			if (args.length != 3) {
				System.out.println("Funcion \"del\" needs two arguments. Example usage: del domain username");
				System.exit(0);
			}
			__deletePassword(args[1], args[2]);
		}
		
		/*
		 * @param String args[1] - domain
		 */
		if (args[0].equals("delall")) {
			if (args.length != 2) {
				System.out.println("Funcion \"delall\" needs one argument. Example usage: delall domain");
				System.exit(0);
			}
			__deleteAllPasswords(args[1]);
		}
		
		/*
		 */
		if (args[0].equals("list")) {
			if (args.length != 1) {
				System.out.println("Funcion \"list\" needs no arguments. Example usage: list");
				System.exit(0);
			}
			__listDomains();
		}
		
		/*
		 * @param String args[1] - domain
		 */
		if (args[0].equals("look")) {
			if (args.length != 2) {
				System.out.println("Funcion \"look\" needs one argument. Example usage: look domain");
				System.exit(0);
			}
			__lookDomain(args[1]);
		}
		
		System.out.println("Available commands:");
		System.out.println("\tset <domain> <username> - adds a new password for <username> at <domain>. Overrides any existing");
		System.out.println("\tget <domain> <username> - gets the password for <username> at <domain>");
		System.out.println("\tgetall <domain> - gets all passwords at <domain>");
		System.out.println("\tdel <domain> <username> - deletes the password for <username> at <domain>");
		System.out.println("\tdelall <domain> - deletes all passwords at <domain>");
		System.out.println("\tlist - lists all domains and usernames stored in the file");
		System.out.println("\tlook <*domain*> - gets all passwords at <domain> by wildcard");
	}
	
	private static void __getPassword(String domain, String username) {
		Console console = System.console();
		Boolean exists = false;
		int i = 0;
		
		masterPass = new String(console.readPassword("Enter your master password for \"passman.store\" file: "));
		
		__parsePasswords();
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase()) && passwords.get(i)[1].toLowerCase().equals(username.toLowerCase())) {
				exists = true;
				break;
			}
		}
		
		if (exists) {
			System.out.println(":: Password for \"" + username + "\" @ \"" + domain + "\": " + passwords.get(i)[2] + "\n");
		} else {
			System.out.println(":: No password found for \"" + username + "\" @ \"" + domain + "\"");
		}
		
		System.exit(0);
	}
	
	private static void __getAllPasswords(String domain) {
		Console console = System.console();
		List<String[]> pwds = new ArrayList<String[]>();
		int i = 0;
		
		masterPass = new String(console.readPassword("Enter your master password for \"passman.store\" file: "));
		
		__parsePasswords();
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase())) {
				pwds.add(passwords.get(i));
			}
		}
		
		if (pwds.size() > 0) {
			System.out.println(":: " + Integer.toString(pwds.size()) + " password(s) found for domain \"" + domain + "\":");
			for (i = 0;i < pwds.size();i++) {
				System.out.println(pwds.get(i)[1] + " : " + pwds.get(i)[2]);
			}
			System.out.println("");
		} else {
			System.out.println(":: No passwords found for domain " + domain);
		}
		
		System.exit(0);
	}
	
	private static void __addPassword(String domain, String username) {
		Console console = System.console();
		String strOut = "";
		Boolean exists = false;
		int i = 0;
		
		if (console == null) {
			System.out.println(":: Couldn't get Console instance");
			System.exit(0);
		}
	
		masterPass = new String(console.readPassword("Enter your master password for \"passman.store\" file: "));
		char pwd1[] = console.readPassword("Enter your password for \"" + username + "\" @ \"" + domain + "\": ");
		char pwd2[] = console.readPassword("Retype password for \"" + username + "\" @ \"" + domain + "\": ");
	
		if (!Arrays.equals(pwd1, pwd2)) {
			System.out.println(":: Password mismatch");
			System.exit(0);
		}
		
		__parsePasswords();
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase()) && passwords.get(i)[1].toLowerCase().equals(username.toLowerCase())) {
				exists = true;
				break;
			}
		}
		if (exists == true) {
			passwords.set(i, new String[] {domain, username, new String(pwd1)});
		} else {
			passwords.add(new String[] {domain, username, new String(pwd1)});
		}
		
		for (i = 0; i < passwords.size(); i++) {
			strOut += String.join("\t", passwords.get(i)) + "\n";
		}
//System.out.println(strOut);
		try {
			__writePasswords(__encrypt(strOut.trim()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
		System.out.println(":: Password set");
		
		System.exit(0);
	}
	
	private static void __deletePassword(String domain, String username) {
		Console console = System.console();
		String confirm, strOut = "";
		int i = 0;
		
		confirm = new String(console.readLine("Are you sure you want to delete the password for \"" + username + "\" @ \"" + domain + "\"? [N/y]"));

		if (!confirm.equals("y")) {
			System.exit(0);
		}
		
		masterPass = new String(console.readPassword("Enter your master password for \"passman.store\" file: "));
		
		__parsePasswords();
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase()) && passwords.get(i)[1].toLowerCase().equals(username.toLowerCase())) {
				passwords.remove(i);
				break;
			}
		}
		
		for (i = 0; i < passwords.size(); i++) {
			strOut += String.join("\t", passwords.get(i)) + "\n";
		}

		try {
			__writePasswords(__encrypt(strOut.trim()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
		System.out.println(":: Password deleted");
		
		System.exit(0);
	}
	
	private static void __deleteAllPasswords(String domain) {
		Console console = System.console();
		String confirm, strOut = "";
		int i = 0;
		
		confirm = new String(console.readLine("Are you sure you want to delete all password for domain \"" + domain + "\"? [N/y]"));

		if (!confirm.equals("y")) {
			System.exit(0);
		}
		
		masterPass = new String(console.readPassword("Enter your master password for \"passman.store\" file: "));
		
		__parsePasswords();
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase())) {
				passwords.remove(i);
			}
		}
		
		for (i = 0; i < passwords.size(); i++) {
			strOut += String.join("\t", passwords.get(i)) + "\n";
		}

		try {
			__writePasswords(__encrypt(strOut.trim()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
		System.out.println(":: Passwords deleted");
		
		System.exit(0);
	}
	
	private static void __listDomains() {
		Console console = System.console();
		int i = 0;
		
		masterPass = new String(console.readPassword("Enter your master password for \"passman.store\" file: "));
		
		__parsePasswords();
		
        Collections.sort(passwords, new Comparator<String[]>() {
            public int compare(String[] p1, String[] p2) {
                return p1[0].compareTo(p2[0]);
            }
        });
	
		System.out.println(":: " + Integer.toString(passwords.size()) + " password(s) stored in \"passman.store\":");
		for (i = 0; i < passwords.size(); i++) {
			System.out.println(passwords.get(i)[0] + " : " + passwords.get(i)[1]);
		}
		System.out.println();

		System.exit(0);
	}
	
	private static void __lookDomain(String domainc) {
		Console console = System.console();
		List<String[]> pwds = new ArrayList<String[]>();
		int i = 0;
		
		masterPass = new String(console.readPassword("Enter your master password for \"passman.store\" file: "));
		
		__parsePasswords();
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().indexOf(domainc.toLowerCase()) > -1) {
				pwds.add(passwords.get(i));
			}
		}
		
		if (pwds.size() > 0) {
			System.out.println(":: " + Integer.toString(pwds.size()) + " password(s) found for domain \"*" + domainc + "*\":");
			for (i = 0;i < pwds.size();i++) {
				System.out.println(pwds.get(i)[0] + " > " + pwds.get(i)[1] + " : " + pwds.get(i)[2]);
			}
			System.out.println("");
		} else {
			System.out.println(":: No passwords found for domain \"*" + domainc + "*\"");
		}
		
		System.exit(0);
	}
	
	private static void __parsePasswords() {
		int bte, i;
		String contents = "";
		
        try {
			File fin = new File("passman.store");
			if (fin.exists()) {
				byte[] bytes = new byte[(int) fin.length()];
				FileInputStream inStr = new FileInputStream(fin);
				inStr.read(bytes);
				inStr.close();
				
				String[] lines = new String(bytes).split("\n");
				lines[0] = "";
				lines[1] = "";

				try {
					contents = __decrypt(DatatypeConverter.parseBase64Binary(String.join("", lines)));
				} catch (Exception ex) {
					System.out.println(":: File \"passman.store\" is corrupt or master password is incorrect");
					System.exit(0);
				}
				
				String[] pwdList = contents.trim().split("\n");
				for (i = 0; i < pwdList.length; i++) {
					String[] tmpPassword = pwdList[i].split("\t");
					if (tmpPassword.length == 3) {
						passwords.add(tmpPassword);
					} else {
						System.out.println(":: File \"passman.store\" is corrupt or master password is incorrect");
						System.exit(0);
					}
				}
			}

        } catch (Exception x) {
            x.printStackTrace();
        }
	}
	
	private static void __writePasswords(byte[] bout) {
		File fout = new File("passman.store");
		
        if (!fout.exists()) {
            try {
                fout.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
		
        try {
			String[] chunks = DatatypeConverter.printBase64Binary(bout).split("(?<=\\G.{64})");
			
            FileOutputStream outStr = new FileOutputStream(fout);
            outStr.write(("# Passwords' manager storage file. Keep the first two lines!\n\n" + String.join("\n", chunks)).getBytes("UTF-8"));
            outStr.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	
	/*
	 * Source from https://gist.github.com/bricef/2436364
	 *
	 */
	private static byte[] __encrypt(String plainText) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] mdStr = md.digest(masterPass.getBytes("UTF-8"));
		
		Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");
		SecretKeySpec key = new SecretKeySpec(mdStr, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
		
		return cipher.doFinal(plainText.getBytes("UTF-8"));
	}
	
	private static String __decrypt(byte[] cipherBytes) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] mdStr = md.digest(masterPass.getBytes("UTF-8"));
		
		Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");
		SecretKeySpec key = new SecretKeySpec(mdStr, "AES");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
		
		return new String(cipher.doFinal(cipherBytes),"UTF-8");
	}
	

}