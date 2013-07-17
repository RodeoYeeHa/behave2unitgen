Story: The ContactService must not allow to insert more than 100 contacts into the database.

Scenario: Filter

Given the Contacts: 
|lastname|firstname|
|Severin|Carsten|
|Severin|Johan|
|Severin|Pina|
|Severin|Birgit|

When Contacts are subset to *n by firstname

Then the Contacts returned are:
|lastname|firstname|
|Severin|Carsten|
|Severin|Johan|