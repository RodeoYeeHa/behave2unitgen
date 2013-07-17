Story: The system must be able to filter the list of contacts. 

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