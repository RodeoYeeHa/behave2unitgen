Story: The ContactService must not allow to insert more than 100 contacts into the database.

Scenario: Filter

Given the Contacts: 
[
{"firstname":"Carsten", "lastname":"Severin"},
{"firstname":"Pina", "lastname":"Severin"},
{"firstname":"Johan", "lastname":"Severin"},
{"firstname":"Birgit", "lastname":"Severin"}
]

When Contacts are subset to *n by firstname

Then the Contacts returned are:
[
{"firstname":"Carsten", "lastname":"Severin"},
{"firstname":"Johan", "lastname":"Severin"}
]
