Story: The ContactService must not allow to insert more than 100 contacts into the database.

Narrative: 
The maximum number of Contact-Objects in the database must be limited to 100.

Scenario: it is possible to insert 1 dataset after 99 have been inserted before
 
Given 99 datasets
When insert one dataset
Then 100 datasets are stored

Scenario: it is not possible to insert a dataset when 100 have been inserted before
 
Given 100 datasets
When insert one dataset
Then insert fails
 

 
