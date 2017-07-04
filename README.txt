
[JPA 2.0 spec]
The entity class must have a no-arg constructor. It may have other constructors as well. The no-arg constructor must be public or protected.
The entity class must a be top-level class. An enum or interface must not be designated as an entity.
The entity class must not be final. No methods or persistent instance variables of the entity class may be final.
If an entity instance is to be passed by value as a detached object (e.g., through a remote interface), the entity class must implement the Serializable interface.
Both abstract and concrete classes can be entities. Entities may extend non-entity classes as well as entity classes, and non-entity classes may extend entity classes.

[참고]
The entity manager is guaranteed to return the same instance of a given entity every time you ask for it
I consider it a good practice for when entities or added to Sets. You could decide to only implement equals when entities will be added to Sets
