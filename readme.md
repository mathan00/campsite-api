Campsite-Api Read Me
--------------------------------------------------------------------------------
The Api allows users to reserve campsite for a minimum 1 to 3 days. 
The User can book upto 1 month in Advance


Swagger URL
--------------------------------------------------------------------------------
Below is the swagger url
http://localhost:8080/swagger-ui/



Design Notes
When concurrent users will try to update at the same time, 
There is Unique constraint is placed on the reservation table for the Start_Column.

multi-spring-boot-app-enabled, this property will enable extra validator on db 
which will prevent any two reservations to made at the db layer. 




