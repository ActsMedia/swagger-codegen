# ActsMedia related README

## Setup

### CoreData

The CoreData infrastructure connects with the Swift Package at: https://apps.actsmedia.com/stash/scm/iosl/amswiftcomponents.git

Specifically the database needs the EntityUtilities target.

## Database Codegen Custom Spec Attributes

### Model

Custom attributes available at the level of a Model Type

(use the `x-` identifier in the comments for the API Spec definition)

```Java
    // Whether this model should be built during database generation
    public Boolean isDatabaseModel; // x-database-model
    // The Type name for the database model
    public String databaseModelName; // x-database-model-name

    // Protocols

    // Indicates whether this model has a property that should be used as primary ID when linking/identifying instances of this Type
    public Boolean databaseIsIdentifiable; // x-database-is-identifiable

    // Indicates whether this model has database relationships that should be generated
    public Boolean databaseShouldGenerateRelationLinks; // x-database-should-generate-relation-links

    // Whether an adherence should be generated for SoftDeletable
    public Boolean isProtocolSoftDeletableType; // x-protocol-soft-deleteable-type
```

### Properties

Custom attributes available at the level of a Property

(use the `x-` text in the comments in the API Spec)

```Java
    // Whether to index the property in the database
    public Boolean databaseIsIndexed; // x-database-is-indexed

    // True if the property indicates a to-one relationship
    public Boolean databaseToOneRelation; // x-database-to-one-relation

    // True if this property defines a to-many relationship
    public Boolean databaseToManyRelation; // x-database-to-many-relation

    // True if this property defines a many-to-many relationship
    public Boolean databaseManyToManyRelation; // x-database-many-to-many-relation

    // The table/model name the property relates to.
    public String databaseRelationModelType; // x-database-relation-model-type

    // The relation name for the referenced table (reference to Category object might be categories)
    public String databaseRelationPropertyName; // x-database-relation-property-name

    // The inverse relation field. sometimes needed, e.g. for CoreData
    public String databaseRelationForeignPropertyName; // x-database-relation-foreign-property-name

    // Whether this table should link to a core data table. For example, we typically only want linking in one direction. 
    // For example, employees might load after stores, so during the employee setup, we link to stores, but when building stores, we don't try to link to employees because they haven't been loaded yet.
    public Boolean databaseShouldGenerateRelationLinks; // x-database-create-relation-link-methods

    // Indicates this property is only needed for database generation, and not necessary for API calls
    public Boolean databaseRelationOnlyProperty; // x-database-relation-only-property

    //Protocol Additions

    // This property keeps tracks of whether the object has been soft-deleted on the server.
    public Boolean isSoftDeletableProperty; // x-is-soft-deletable-property

    // Indicates the property should be used as primary ID when linking/identifying instances of this Type
    // There should only be one ID property per model or generated code will not compile.
    public Boolean isIDProperty; // x-database-id-property

    // Indicates the related model is included directly as nested JSON, not linked by ID.
    public Boolean isNestedModelRelation; // x-database-nested-model-relation
```

## Usage

### Basic

The minimum attributes needed to properly generate a database model are the following

- `x-database-model` to indicate it's a database model
- `x-database-model-name` to define the name used for the Database model Type

### Relationships

To model a database relationship, you need the following attached the Property

- One of `x-database-to-one-relation`, `x-database-many-to-one-relation`, or `x-database-many-to-many-relation`
- `x-database-relation-model-type` defines the name of the Type you're connecting to
- `x-database-relation-property-name` defines the name of the relation property
- `x-database-relation-foreign-property-name` defines the name of the reverse of the connection's property
