# Composite Key Pitfalls and Best Practices

## Potential Pitfalls

1. **Equals and HashCode Implementation**
   - Composite keys must have properly implemented `equals()` and `hashCode()` methods.
   - Missing or incorrect implementations can lead to unexpected behavior in collections and queries.

2. **Serializable Interface**
   - All composite key classes must implement `Serializable`.
   - This is required for JPA to properly serialize and deserialize the keys.

3. **Immutability**
   - Ideally, composite keys should be immutable to prevent issues with entity identity.
   - Changing a key field after an entity is persisted can lead to orphaned records.

4. **Mapping Relationships**
   - When mapping relationships with entities that have composite keys, all parts of the key must be properly mapped.
   - Use `@JoinColumns` instead of `@JoinColumn` to map multiple columns.

5. **@MapsId Usage**
   - When using `@MapsId` with composite keys, specify which part of the composite key is being mapped.
   - Example: `@MapsId("ticketId")` maps to the `ticketId` field in the composite key.

6. **Query Complexity**
   - Queries involving composite keys can be more complex.
   - Use repository methods with appropriate parameter names to simplify queries.

7. **Performance Considerations**
   - Composite keys can impact performance, especially with large datasets.
   - Consider using surrogate keys for high-performance requirements.

8. **Foreign Key Constraints**
   - Foreign key constraints with composite keys require all parts of the key to be included.
   - This can lead to more complex database schemas.

## Best Practices

1. **Use Embeddable Classes**
   - Use `@Embeddable` classes for composite keys to encapsulate key logic.
   - This improves code organization and reusability.

2. **Consistent Naming**
   - Use consistent naming conventions for key fields across entities.
   - This makes relationships easier to understand and maintain.

3. **Validation**
   - Validate all parts of composite keys before persisting entities.
   - This prevents invalid keys from being stored in the database.

4. **Documentation**
   - Document the structure and purpose of composite keys.
   - This helps other developers understand the data model.

5. **Testing**
   - Thoroughly test operations involving composite keys.
   - This includes CRUD operations, queries, and relationships.

# IntelliJ Run Configuration Verification

When running the application in IntelliJ IDEA, verify the following:

1. **JDK Version**
   - Ensure Java 17 is selected in the run configuration.
   - Project structure settings should also use Java 17.

2. **Spring Boot Configuration**
   - Use the Spring Boot run configuration type.
   - Set the main class to `de.bachelorarbeit.ticketsystem.TicketSystemApplication`.

3. **Environment Variables**
   - Set any necessary environment variables in the run configuration.
   - For development, you might want to set `spring.profiles.active=dev`.

4. **VM Options**
   - For development, consider adding `-Dspring.output.ansi.enabled=always` for colored console output.

5. **PostgreSQL Connection**
   - Ensure PostgreSQL is running and accessible.
   - Verify the connection details in `application.properties` match your PostgreSQL instance.

6. **Before Launch Tasks**
   - Configure "Build" as a before-launch task to ensure the latest code is compiled.

7. **Working Directory**
   - Set the working directory to the project root.

8. **Module Classpath**
   - Ensure the module classpath includes all necessary dependencies.

9. **Application Logs**
   - Check the console for application logs during startup.
   - Look for any errors or warnings that might indicate configuration issues.

10. **Database Initialization**
    - If using `spring.jpa.hibernate.ddl-auto=create` or `create-drop`, be aware that this will recreate the database schema on startup.
    - For production, use `update` or `validate` instead.