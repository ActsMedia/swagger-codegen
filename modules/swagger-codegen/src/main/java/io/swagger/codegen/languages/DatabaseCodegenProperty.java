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
    // Ther should only be one ID property per model or generated code will not compile.
    public Boolean isIDProperty; // x-database-id-property

    // Indicates the related model is included directly as nested JSON, not linked by ID.
    public Boolean isNestedModelRelation; // x-database-nested-model-relation

    //Testing Additions

    //Ability to exclude something from auto-gen tests
    public Boolean isExcludedFromTests; //x-exclude-from-tests

    //convenience computed properties
    
    public Boolean databasePropertyIsToManyRelationship;
    public Boolean databasePropertyIsRelationship;

    //Other

    public String nameInCamelCaseFirstLetterLower;
    public String datatypeForXCDataModel;
    

    public void processCustomProperties(){
        
        // Check JSON for custom code
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> jsonData = mapper.readValue(this.jsonSchema, Map.class);

            //Database 

            this.databasePropertyIsToManyRelationship = false;
            this.databasePropertyIsRelationship = false;
            
            this.databaseIsIndexed = (Boolean) jsonData.get("x-database-is-indexed");
            this.databaseToOneRelation = (Boolean) jsonData.get("x-database-to-one-relation");
            this.databaseToManyRelation = (Boolean) jsonData.get("x-database-to-many-relation");
            this.databaseManyToManyRelation = (Boolean) jsonData.get("x-database-many-to-many-relation");
            this.isNestedModelRelation = (Boolean) jsonData.get("x-database-nested-model-relation");

            if((this.databaseToManyRelation != null && this.databaseToManyRelation) || (this.databaseManyToManyRelation != null && this.databaseManyToManyRelation)) {
                this.databasePropertyIsToManyRelationship = true;
            }
            if((this.databaseToOneRelation != null && this.databaseToOneRelation) || (this.databaseToManyRelation != null && this.databaseToManyRelation) || (this.databaseManyToManyRelation != null && this.databaseManyToManyRelation)) {
                this.databasePropertyIsRelationship = true;
            }
            this.databaseRelationModelType = (String) jsonData.get("x-database-relation-model-type");
            this.databaseRelationPropertyName = (String) jsonData.get("x-database-relation-property-name");
            this.databaseRelationForeignPropertyName = (String) jsonData.get("x-database-relation-foreign-property-name");

            this.databaseShouldGenerateRelationLinks = (Boolean) jsonData.get("x-database-create-relation-link-methods");
            this.databaseRelationOnlyProperty = (Boolean) jsonData.get("x-database-relation-only-property");
            
            //Protocols
            this.isSoftDeletableProperty = (Boolean) jsonData.get("x-is-soft-deletable-property");
            this.isIDProperty = (Boolean) jsonData.get("x-database-id-property");

            //Testing
            this.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");

            //Other
            this.nameInCamelCaseFirstLetterLower = DefaultCodegen.camelize(name, true);

            if(datatype != null) {
                switch (datatype) {
                    case "array":
                    case "List":
                    case "map":
                    case "Array":
                    case "Dictionary":
                        this.datatypeForXCDataModel = "Transformable";
                        break;
                    case "date":
                    case "Date":
                    case "DateTime":
                        this.datatypeForXCDataModel = "Date";
                        break;
                    case "Bool":
                    case "boolean":
                        this.datatypeForXCDataModel = "Boolean";
                        break;
                    case "string":
                    case "char":
                    case "object":
                        this.datatypeForXCDataModel = "String";
                        break;
                    case "short":
                    case "Int32":
                        this.datatypeForXCDataModel = "Integer 32";
                        break;
                    case "Int":
                    case "int":
                    case "Int64":
                    case "long":
                    case "integer":
                    case "Integer":
                        this.datatypeForXCDataModel = "Integer 64";
                        break;
                    case "Double":
                    case "float":
                    case "number":
                    case "double":
                        this.datatypeForXCDataModel = "Double";
                        break;
                    case "file":
                    case "URL":
                    case "Data":
                        this.datatypeForXCDataModel = "Binary";
                        break;
                    default:
                        this.datatypeForXCDataModel = this.datatype;
                        break;
                }
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}