# Passman
A very simple console password manager written in Java.

<br /><br />
##### File mode
Edit /dv/utils/config.txt file. Put "FILE" as "mode" and some filename after "filename". Put it into dv/utils/ folder.
Then build sources and compile JAR file:
```
>>> javac Passman.java -d .
>>> javac PassmanCRUD.java -d .
>>> javac PassmanTransport.java -d .
>>> jar -cvfm Passman.jar Manifest.txt dv/utils/*
```

<br /><br />
##### Firebase mode
The application can use Firebase databse to store passwords in it.
Set up your account: https://www.firebase.com/ and create a "secret". Edit the rules and allow only authenticated requests:
```
{
  "rules": {
    "passman": {
      "anastas": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```
Create a DB and user. Make sure you put the endpoints correctly in the config.txt file. The application appends "json" to the endpoint as well as your secret.
Then build sources and compile JAR file:
```
>>> javac Passman.java -d .
>>> javac PassmanCRUD.java -d .
>>> javac PassmanTransport.java -d .
>>> jar -cvfm Passman.jar Manifest.txt dv/utils/*
```

<br /><br />
##### Example usage
```Java
// Adds a new password for <username> at <domain>. Overrides any existing:
>>> java -jar Passman.jar set MyDomain MyUsername
>>> ....

// Gets the password for <username> at <domain>:
>>> java -jar Passman.jar get MyDomain MyUsername
>>> ....

// Deletes the password for <username> at <domain>:
>>> java -jar Passman.jar del MyDomain MyUsername
>>> ....

// Gets all passwords at <domain>:
>>> java -jar Passman.jar getall MyDomain
>>> ....

// Deletes all passwords at <domain>:
>>> java -jar Passman.jar delall MyDomain MyUsername
>>> ....

// Lists all domains and usernames stored in the file:
>>> java -jar Passman.jar list
>>> ....

// Gets all passwords at <domain> by wildcard:
>>> java -jar Passman.jar look MyDomain
>>> ....

// Exports all passwords to a file:
>>> java -jar Passman.jar export FileName
>>> ....

// Imports passwords from a tab-delimited file:
>>> java -jar Passman.jar import FileName
>>> ....
```
