## SharkORM
# Description
A java based ORM project. Persists objects to a database without the need for manual SQL.

# Project Status
Currently incomplete

# Features
Annotation based persistence of objects
Perform persistence operation without the need for manual SQL calls
Straightforward implementation

# Installation
1. Clone the project and mvn install
2. Add the dependency to the pom.xml file of the project using it
3. Configure applications.properties

# Annotations
Classes are persisted based off Annotations

@Entity(name = "example_name")
Associates the object with the database table "example_name". This annotation must be present for any other annotations to function.


@ID(name = "id_name")
Informs the ORM that the given field is the ID of the object. The field is set as the primary key of the table. If no name is specified the ORM will default to the name of the field.

@Column(name = "column_name")
Associates the given field with the database column "column_name". This annotation allows the persistence of the properties of objects. If no name is specified the ORM will default to the name of the field.
