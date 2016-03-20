package dv.utils;

import java.io.Console;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Passman {
	private static PassmanTransport ptrans = new PassmanTransport();
	private static passMode pmode;
	private static String pfilename;
	private static String purl;
	private static String puser;
	private static String pkey;
	private static String[] passData;
	
	private enum passMode {
		FILE, FIREBASE		
	}
	
	/*
	 * @param String args[0] - set/get/del/getall/delall/list/look
	 * 
	 *
	 */
	public static void main(String[] args) {
		Console console = System.console();
		
		getConfig();

		switch (pmode) {
			case FILE:
				passData = ptrans.readFromFile(pfilename);
				break;
			case FIREBASE:
				passData = ptrans.readFromFirebase(purl, puser, pkey);
				break;
		}
		
		if (args.length == 0) {
			System.out.println("Available commands:");
			System.out.println("\tset <domain> <username> - adds a new password for <username> at <domain>. Overrides any existing");
			System.out.println("\tget <domain> <username> - gets the password for <username> at <domain>");
			System.out.println("\tgetall <domain> - gets all passwords at <domain>");
			System.out.println("\tdel <domain> <username> - deletes the password for <username> at <domain>");
			System.out.println("\tdelall <domain> - deletes all passwords at <domain>");
			System.out.println("\tlist - lists all domains and usernames stored in the file");
			System.out.println("\tlook <*domain*> - gets all passwords at <domain> by wildcard\n");
			
			System.exit(0);
		}
		/*
		 * @param String args[1] - domain
		 * @param String args[2] - username
		 */
		if (args[0].equals("get")) {
			if (args.length != 3) {
				System.out.println("Command \"get\" needs two arguments. Example usage: get domain username\n");
				System.exit(0);
			}
			
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			String[] password = pcrud.getPassword(args[1], args[2]);

			if (password[0] != null) {
				System.out.println(":: Password for \"" + password[1] + "\" @ \"" + password[0] + "\": " + password[2] + "\n");
			} else {
				System.out.println(":: No password found for \"" + args[2] + "\" @ \"" + args[1] + "\"\n");
			}
			
			System.exit(0);
		}
		
		/*
		 * @param String args[1] - domain
		 * @param String args[2] - username
		 */
		if (args[0].equals("set")) {
			if (args.length != 3) {
				System.out.println("Command \"set\" needs two arguments. Example usage: set domain username\n");
				System.exit(0);
			}
			
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			
			if (pcrud.errCode == 1) {
				String confirmOverwrite = new String(console.readLine("The destination source was decrypted with errors. If you continue any existing passwords will be lost. Do you want to continue? [N/y]"));
				
				if (!confirmOverwrite.equals("y")) {
					System.exit(0);
				}
			}
			
			char pwd1[] = console.readPassword("Enter your password for \"" + args[2] + "\" @ \"" + args[1] + "\": ");
			char pwd2[] = console.readPassword("Retype password for \"" + args[2] + "\" @ \"" + args[1] + "\": ");

			if (!Arrays.equals(pwd1, pwd2)) {
				System.out.println(":: Password mismatch\n");
				System.exit(0);
			}
			
			pcrud.setPassword(new String(args[1]), new String(args[2]), new String(pwd1));

			switch (pmode) {
				case FILE:
					ptrans.writeToFile(pfilename, pcrud.IV, pcrud.PEM);
					break;
				case FIREBASE:
					ptrans.writeToFirebase(purl, puser, pkey, pcrud.IV, pcrud.PEM);
					break;
			}
			
			System.out.println(":: Password set\n");
			System.exit(0);
		}
		
		/*
		 * @param String args[1] - domain
		 */
		if (args[0].equals("getall")) {
			if (args.length != 2) {
				System.out.println("Command \"getall\" needs one argument. Example usage: getall domain\n");
				System.exit(0);
			}
			
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			List<String[]> pwds = pcrud.getAllPasswords(args[1]);
			
			if (pwds.size() == 0) {
				System.out.println(":: No passwords found for domain \"" + args[1] + "\"\n");
			} else {
				System.out.println(":: " + Integer.toString(pwds.size()) + " password(s) found for domain \"" + pwds.get(0)[0] + "\":");
				for (int i = 0;i < pwds.size();i++) {
					System.out.println(pwds.get(i)[1] + " : " + pwds.get(i)[2]);
				}
				System.out.println("");
			}
			
			System.exit(0);
		}
		
		/*
		 * @param String args[1] - domain
		 * @param String args[2] - username
		 */
		if (args[0].equals("del")) {
			if (args.length != 3) {
				System.out.println("Command \"del\" needs two arguments. Example usage: del domain username\n");
				System.exit(0);
			}
			
			String confirm = new String(console.readLine("Are you sure you want to delete the password for \"" + args[2] + "\" @ \"" + args[1] + "\"? [N/y]"));
	
			if (!confirm.equals("y")) {
				System.exit(0);
			}
		
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			
			if (pcrud.errCode == 1) {
				String confirmOverwrite = new String(console.readLine("The destination source was decrypted with errors. If you continue any existing passwords will be lost. Do you want to continue? [N/y]"));
				
				if (!confirmOverwrite.equals("y")) {
					System.exit(0);
				}
			}
			
			pcrud.deletePassword(args[1], args[2]);
			
			switch (pmode) {
				case FILE:
					ptrans.writeToFile(pfilename, pcrud.IV, pcrud.PEM);
					break;
				case FIREBASE:
					ptrans.writeToFirebase(purl, puser, pkey, pcrud.IV, pcrud.PEM);
					break;
			}
			
			System.out.println(":: Password deleted\n");		
			System.exit(0);
		}
		
		/*
		 * @param String args[1] - domain
		 */
		if (args[0].equals("delall")) {
			if (args.length != 2) {
				System.out.println("Command \"delall\" needs one argument. Example usage: delall domain\n");
				System.exit(0);
			}
			
			String confirm = new String(console.readLine("Are you sure you want to delete all password for domain \"" + args[1] + "\"? [N/y]"));
	
			if (!confirm.equals("y")) {
				System.exit(0);
			}
		
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			
			if (pcrud.errCode == 1) {
				String confirmOverwrite = new String(console.readLine("The destination source was decrypted with errors. If you continue any existing passwords will be lost. Do you want to continue? [N/y]"));
				
				if (!confirmOverwrite.equals("y")) {
					System.exit(0);
				}
			}
			
			pcrud.deleteAllPasswords(args[1]);
			
			switch (pmode) {
				case FILE:
					ptrans.writeToFile(pfilename, pcrud.IV, pcrud.PEM);
					break;
				case FIREBASE:
					ptrans.writeToFirebase(purl, puser, pkey, pcrud.IV, pcrud.PEM);
					break;
			}
			
			System.out.println(":: Passwords deleted\n");		
			System.exit(0);
		}
		
		/*
		 */
		if (args[0].equals("list")) {
			if (args.length != 1) {
				System.out.println("Command \"list\" needs no arguments. Example usage: list\n");
				System.exit(0);
			}
			
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			List<String[]> pwds = pcrud.listDomains();
			
			if (pwds.size() == 0) {
				System.out.println(":: No passwords found\n");
			} else {
				System.out.println(":: " + Integer.toString(pwds.size()) + " password(s) stored:");
				for (int i = 0;i < pwds.size();i++) {
					System.out.println(pwds.get(i)[0] + " : " + pwds.get(i)[1]);
				}
				System.out.println("");
			}
			
			System.exit(0);
		}
		
		/*
		 * @param String args[1] - domain
		 */
		if (args[0].equals("look")) {
			if (args.length != 2) {
				System.out.println("Command \"look\" needs one argument. Example usage: look domain\n");
				System.exit(0);
			}
			
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			List<String[]> pwds = pcrud.lookDomain(args[1]);
			
			if (pwds.size() == 0) {
				System.out.println(":: No passwords found for domain \"*" + args[1] + "*\"\n");
			} else {
				System.out.println(":: " + Integer.toString(pwds.size()) + " password(s) found for domain \"*" + args[1] + "*\":");
				for (int i = 0;i < pwds.size();i++) {
					System.out.println(pwds.get(i)[0] + " > " + pwds.get(i)[1] + " : " + pwds.get(i)[2]);
				}
				System.out.println("");
			}
			
			System.exit(0);
		}
		
		if (args[0].equals("export")) {
			if (args.length != 2) {
				System.out.println("Command \"export\" needs one argument. Example usage: export name-of-tab-separated-file\n");
				System.exit(0);
			}
			
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			ptrans.exportToFile(new String(args[1]), pcrud.exportPasswords());
			
			System.out.println(":: Passwords exported to \"" + args[1] + "\"\n");
			
			System.exit(0);
		}
		
		if (args[0].equals("import")) {
			if (args.length != 2) {
				System.out.println("Command \"import\" needs one argument. Example usage: export filename\n");
				System.exit(0);
			}
			
			String masterPass = new String(console.readPassword("Enter your master password: "));
			PassmanCRUD pcrud = new PassmanCRUD(masterPass, passData[0], passData[1]);
			
			if (pcrud.errCode == 1) {
				String confirmOverwrite1 = new String(console.readLine("The destination source was decrypted with errors. If you continue any existing passwords will be lost. Do you want to continue? [N/y]"));
				
				if (!confirmOverwrite1.equals("y")) {
					System.exit(0);
				}
			}
			
			String confirmOverwrite2 = new String(console.readLine("WARNING!. In case of username collisions, the existing passwords will be overwritten. Do you want to continue? [N/y]"));
			
			if (!confirmOverwrite2.equals("y")) {
				System.exit(0);
			}
			
			pcrud.importPasswords(ptrans.importFromFile(new String(args[1])));
			
			switch (pmode) {
				case FILE:
					ptrans.writeToFile(pfilename, pcrud.IV, pcrud.PEM);
					break;
				case FIREBASE:
					ptrans.writeToFirebase(purl, puser, pkey, pcrud.IV, pcrud.PEM);
					break;
			}
			
			System.out.println(":: Passwords imported from \"" + args[1] + "\"\n");
			
			System.exit(0);
		}
		
		System.out.println("Available commands:");
		System.out.println("\tset <domain> <username> - adds a new password for <username> at <domain>. Overwrites any existing");
		System.out.println("\tget <domain> <username> - gets the password for <username> at <domain>");
		System.out.println("\tgetall <domain> - gets all passwords at <domain>");
		System.out.println("\tdel <domain> <username> - deletes the password for <username> at <domain>");
		System.out.println("\tdelall <domain> - deletes all passwords at <domain>");
		System.out.println("\tlist - lists all domains and usernames stored in the file");
		System.out.println("\tlook <*domain*> - gets all passwords at <domain> by wildcard\n");
	}
	
	private static void getConfig() {
		int i;
		
		List<String[]> config = ptrans.readConfig();
		
		for (i = 0;i < config.size();i++) {
			switch (config.get(i)[0]) {
				case "mode":
					pmode = passMode.valueOf(config.get(i)[1]);
					break;
				case "filename":
					pfilename = config.get(i)[1];
					break;
				case "url":
					purl = config.get(i)[1];
					break;
				case "user":
					puser = config.get(i)[1];
					break;
				case "key":
					pkey = config.get(i)[1];
					break;
			}
		}
	}
}