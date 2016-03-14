# Passman
A very simple console password manager written in Java.

Creates a "passman.store" file and puts passwords in it, encrypted with AES/CBC and protected by a master password.

Example usage:
```Java
// Adds a new password for <domain> and <username>. Overrides existing:
>>> java dv/utils/Passman set MyDomain MyUsername
>>> ....

// Gets the password for <domain> and <username>:
>>> java dv/utils/Passman get MyDomain MyUsername
>>> ....

// Deletes the password for <domain> and <username>:
>>> java dv/utils/Passman del MyDomain MyUsername
>>> ....

// Gets all passwords for <domain>:
>>> java dv/utils/Passman getall MyDomain
>>> ....

// Deletes all passwords for <domain>:
>>> java dv/utils/Passman delall MyDomain MyUsername
>>> ....

// Lists all domains and usernames:
>>> java dv/utils/Passman list
>>> ....
```

Remove the first line of code and recompile it for simpler usage.