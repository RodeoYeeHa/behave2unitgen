Story: The system must be able to filter the list of contacts. 

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
