Story: 
After renaming the last name of a contact, the System must display the new name of the contact. furthermore, 
the system has to make shure, that all other contacts keep their lastnames.

Scenario: One of two contacts is renamed

Given the Contacts: 
|lastname|firstname|
|Severin|Carsten|
|Raschdorf|Birgit|

When the contact is renamed: {"firstnameOld":"Birgit", "lastnameOld":"Raschdorf", "firstnameNew":"Birgit", "lastnameNew":"Severin"}

Then the Contacts returned are:
|lastname|firstname|
|Severin|Carsten|
|Severin|Birgit|