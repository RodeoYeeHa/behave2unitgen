Story: 
The system must allow the user to delete any of his contacts.
The user is not allowed to delete contacts of other owners.

Scenario: "Delete by Lastname" only deletes contacts of the current user itself

Given the Contacts: 
|lastname|firstname|owner|
|Severin|Carsten|umeier|
|Schmidt|Pina|umeier|
|Severin|Carsten|hschmidt|

When Contact {"lastname":"Severin", "owner":"umeier"} is deleted

Then the Contacts are:
|lastname|firstname|owner|
|Schmidt|Pina|umeier|
|Severin|Carsten|hschmidt|


Scenario: "Delete by Lastname" when no such contact exists for the user

Given the Contacts: 
|lastname|firstname|owner|
|Severin|Carsten|hschmidt|

When Contact {"lastname":"Severin", "owner":"umeier"} is deleted

Then the Contacts are:
|lastname|firstname|owner|
|Severin|Carsten|hschmidt|