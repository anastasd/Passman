package dv.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.security.MessageDigest;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class PassmanCRUD {
	public String masterPass;
	public String IV;
	public String PEM;
	public List<String[]> passwords = new ArrayList<String[]>();
	public Integer errCode = 0;
	public String errMsg;
	
	public PassmanCRUD(String mpassword, String initVector, String pemCode){
		String contents = "";
		int i;

		try {
			IV = initVector;
			masterPass = mpassword;
			contents = new String(__decrypt(DatatypeConverter.parseBase64Binary(pemCode)));
		} catch (Exception ex) {
			errCode = 1;
			errMsg = "Encrypted source is corrupt or master password is incorrect";
		}

		String[] pwdList = contents.trim().split("\n");
		for (i = 0; i < pwdList.length; i++) {
			String[] tmpPassword = pwdList[i].split("\t");
			if (tmpPassword.length == 3) {
				passwords.add(tmpPassword);
			} else {
				errCode = 1;
				errMsg = "Encrypted source is corrupt or master password is incorrect";
			}
		}
	}
	
	public String[] getPassword(String domain, String username) {
		String[] password = new String[3];
		int i = 0;
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase()) && passwords.get(i)[1].toLowerCase().equals(username.toLowerCase())) {
				password = passwords.get(i);
				break;
			}
		}
		
		return password;
	}
	
	public List<String[]> getAllPasswords(String domain) {
		List<String[]> pwds = new ArrayList<String[]>();
		int i = 0;
		
		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase())) {
				pwds.add(passwords.get(i));
			}
		}
		
		return pwds;
	}
	
	public void setPassword(String domain, String username, String password) {
		Boolean exists = false;
		String strOut = "";
		int i = 0;

		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase()) && passwords.get(i)[1].toLowerCase().equals(username.toLowerCase())) {
				exists = true;
				break;
			}
		}
		if (exists == true) {
			passwords.set(i, new String[] {domain, username, password});
		} else {
			passwords.add(new String[] {domain, username, password});
		}
		
		for (i = 0; i < passwords.size(); i++) {
			strOut += String.join("\t", passwords.get(i)) + "\n";
		}
		
		try {
			PEM = DatatypeConverter.printBase64Binary(__encrypt(strOut.trim()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public void deletePassword(String domain, String username) {
		String strOut = "";
		int i = 0;

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
			PEM = DatatypeConverter.printBase64Binary(__encrypt(strOut.trim()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public void deleteAllPasswords(String domain) {
		String strOut = "";
		int i = 0;

		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().equals(domain.toLowerCase())) {
				passwords.remove(i);
				i--;
			}
		}

		for (i = 0; i < passwords.size(); i++) {
			strOut += String.join("\t", passwords.get(i)) + "\n";
		}
		
		try {
			PEM = DatatypeConverter.printBase64Binary(__encrypt(strOut.trim()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public List<String[]> listDomains() {
		List<String[]> pwds = new ArrayList<String[]>();
		int i = 0;
		
        Collections.sort(passwords, new Comparator<String[]>() {
            public int compare(String[] p1, String[] p2) {
                return p1[0].compareTo(p2[0]);
            }
        });
	
		for (i = 0; i < passwords.size(); i++) {
			String[] password = passwords.get(i);
			pwds.add(new String[] {password[0], password[1]});
		}

		return pwds;
	}
	
	public List<String[]> lookDomain(String domainc) {
		List<String[]> pwds = new ArrayList<String[]>();
		int i = 0;

		for (i = 0; i < passwords.size(); i++) {
			if (passwords.get(i)[0].toLowerCase().indexOf(domainc.toLowerCase()) > -1) {
				pwds.add(passwords.get(i));
			}
		}
		
		return pwds;
	}
	
	/*
	 * Source from https://gist.github.com/bricef/2436364
	 *
	 */
	private byte[] __encrypt(String plainString) throws Exception {
		byte[] newIV = new byte[16];
		new Random().nextBytes(newIV);
		IV = DatatypeConverter.printBase64Binary(newIV);
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] mdStr = md.digest(masterPass.getBytes("UTF-8"));
		
		Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");
		SecretKeySpec key = new SecretKeySpec(mdStr, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(newIV));
		
		return cipher.doFinal(plainString.getBytes("UTF-8"));
	}
	
	private byte[] __decrypt(byte[] cipherBytes) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] mdStr = md.digest(masterPass.getBytes("UTF-8"));
		
		Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");
		SecretKeySpec key = new SecretKeySpec(mdStr, "AES");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(DatatypeConverter.parseBase64Binary(IV)));
		
		return cipher.doFinal(cipherBytes);
	}
}