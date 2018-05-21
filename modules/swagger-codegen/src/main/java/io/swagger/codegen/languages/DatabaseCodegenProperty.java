package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenModelType;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Strings;
import java.util.regex.Pattern;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class DatabaseCodegenProperty extends CodegenProperty {
        
    // Database Additions

    // Whether to index the property in the database
    public Boolean databaseIsIndexed; // x-database-is-indexed

    // Whether the attached object(s) is a reference to another table
    public Boolean databaseIsRelation; // x-database-is-relation

    // Whether the reference is a single uuid (false) or contains an array (true). If it contains an array, the reference will be a "to many" relationship.
    public Boolean databaseRelationIsToManyReference; // x-database-relation-is-to-many-reference
    public Boolean databaseRelationIsManyToManyReference; // x-database-relation-is-many-to-many-reference

    // The table/model name that the property relates to. For example "Category"
    public String databaseRelationModelType; // x-database-relation-model-type

    // The relation name for the referenced table (reference to Category object might be categories)
    public String databaseRelationPropertyName; // x-database-relation-property-name

    // The inverse relation for Core Data
    public String databaseRelationForeignPropertyName; // x-database-relation-foreign-property-name

    // Whether this table should link to a core data table. For example, we typically only want linking in one direction. For example, employees might load after stores, so during the employee setup, we link to stores, but when building stores, we don't try to link to employees because they haven't been loaded yet.
    public Boolean databaseRelationCreateLinkMethods; // x-database-relation-create-link-methods

    // This indicates that this property is only needed for database generation, and not necessary for API calls
    public Boolean databaseRelationOnlyProperty; // x-database-relation-only-property
    

    //Protocol Additions
    
    // This property keeps tracks of whether the object has been soft-deleted on the server.
    public Boolean isSoftDeletableProperty; // x-is-soft-deletable-property

    // This property keeps tracks of whether this is  a UUID property
    public Boolean isUUIDProperty; // x-is-uuid-property


    //Testing Additions

    //Ability to exclude something from auto-gen tests
    public Boolean isExcludedFromTests; //x-exclude-from-tests

    //Other

    public String nameInCamelCaseFirstLetterLower;

    public void processCustomProperties(){
        
        // Check JSON for custom code
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> jsonData = mapper.readValue(this.jsonSchema, Map.class);

            //Database 

            this.databaseIsIndexed = (Boolean) jsonData.get("x-database-is-indexed");
            this.databaseIsRelation = (Boolean) jsonData.get("x-database-is-relation");
            this.databaseRelationIsToManyReference = (Boolean) jsonData.get("x-database-relation-is-to-many-reference");
            this.databaseRelationIsManyToManyReference = (Boolean) jsonData.get("x-database-relation-is-many-to-many-reference");
            
            this.databaseRelationModelType = (String) jsonData.get("x-database-relation-model-type");
            this.databaseRelationPropertyName = (String) jsonData.get("x-database-relation-property-name");
            this.databaseRelationForeignPropertyName = (String) jsonData.get("x-database-relation-foreign-property-name");

            this.databaseRelationCreateLinkMethods = (Boolean) jsonData.get("x-database-relation-create-link-methods");
            this.databaseRelationOnlyProperty = (Boolean) jsonData.get("x-database-relation-only-property");
            
            //Protocols
            this.isSoftDeletableProperty = (Boolean) jsonData.get("x-is-soft-deletable-property");
            this.isUUIDProperty = (Boolean) jsonData.get("x-is-uuid-property");


            //Testing
            this.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");

            //Other
            this.nameInCamelCaseFirstLetterLower = DefaultCodegen.camelize(name, true);

        } catch (IOException e) {}
    }
}