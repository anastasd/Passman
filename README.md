# Passman
A very simple console password manager written in Java.

Creates a file and puts passwords in it, encrypted with AES/CBC and protected by a master password.

Example usage:
```Java
// Adds a new password for <username> at <domain>. Overrides any existing:
>>> java dv/utils/Passman set MyDomain MyUsername
>>> ....

// Gets the password for <username> at <domain>:
>>> java dv/utils/Passman get MyDomain MyUsername
>>> ....

// Deletes the password for <username> at <domain>:
>>> java dv/utils/Passman del MyDomain MyUsername
>>> ....

// Gets all passwords at <domain>:
>>> java dv/utils/Passman getall MyDomain
>>> ....

// Deletes all passwords at <domain>:
>>> java dv/utils/Passman delall MyDomain MyUsername
>>> ....

// Lists all domains and usernames stored in the file:
>>> java dv/utils/Passman list
>>> ....

// Gets all passwords at <domain> by wildcard:
>>> java dv/utils/Passman look MyDomain
>>> ....
```

Remove the first line of code and recompile it for simpler usage.