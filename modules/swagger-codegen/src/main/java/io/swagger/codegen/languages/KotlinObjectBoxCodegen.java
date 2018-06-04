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

public class KotlinObjectBoxCodegen extends AbstractKotlinCodegen {

    protected String invokerPackage = "io.swagger";

    public static final String DATE_LIBRARY = "dateLibrary";
    protected CodegenConstants.ENUM_PROPERTY_NAMING_TYPE enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase;
    static Logger LOGGER = LoggerFactory.getLogger(KotlinObjectBoxCodegen.class);

    protected String dateLibrary = DateLibrary.JAVA8.value;

    public enum DateLibrary {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8");

        public final String value;

        DateLibrary(String value) {
            this.value = value;
        }
    }

    /**
     * Constructs an instance of `KotlinObjectBoxCodegen`.
     */
    public KotlinObjectBoxCodegen() {
        super();

        artifactId = "kotlin-ObjectBox";
        packageName = "io.swagger.client";

        outputFolder = "generated-code" + File.separator + "kotlin-ObjectBox";
        modelTemplateFiles.put("model.mustache", ".kt");
        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        embeddedTemplateDir = templateDir = "kotlin-ObjectBox";
        ///We're not using APIs in the ObjectBox codegen
        // apiTemplateFiles.put("api.mustache", ".kt");
        // apiDocTemplateFiles.put("api_doc.mustache", ".md");
        // apiPackage = packageName + ".apis";
        // modelPackage = packageName + ".models";

        CliOption dateLibrary = new CliOption(DATE_LIBRARY, "Option. Date library to use");
        Map<String, String> dateOptions = new HashMap<>();
        dateOptions.put(DateLibrary.THREETENBP.value, "Threetenbp");
        dateOptions.put(DateLibrary.STRING.value, "String");
        dateOptions.put(DateLibrary.JAVA8.value, "Java 8 native JSR310");
        dateLibrary.setEnum(dateOptions);
        cliOptions.add(dateLibrary);

        typeMapping.put("array", "kotlin.collections.List");
        typeMapping.put("list", "kotlin.collections.List");
        typeMapping.put("binary", "kotlin.collections.List<kotlin.Byte>");

        instantiationTypes.put("array", "listOf");
        instantiationTypes.put("list", "listOf");

        importMapping.put("SoftDeletable", "com.mmm.thinbonding.Model.swagger.database.SoftDeletable");
        importMapping.put("UUIDEntity", "com.mmm.thinbonding.Model.swagger.database.UUIDEntity");
        importMapping.put("annotations", "io.objectbox.annotation.*");
        importMapping.put("BoxFor", "import io.objectbox.kotlin.boxFor");
        importMapping.put("ToMany", "io.objectbox.relation.ToMany");
        importMapping.put("ToOne", "io.objectbox.relation.ToOne");
        importMapping.put("Parcelable", "android.os.Parcelable");
        importMapping.put("IgnoredOnParcel", "kotlinx.android.parcel.IgnoredOnParcel");
        importMapping.put("Parcelize", "kotlinx.android.parcel.Parcelize");
        importMapping.put("Serializable", "java.io.Serializable");
        importMapping.put("SerializedName", "com.google.gson.annotations.SerializedName");
        
        
        

        CodegenModelFactory.setTypeMapping(CodegenModelType.PROPERTY, DatabaseCodegenProperty.class);
        CodegenModelFactory.setTypeMapping(CodegenModelType.MODEL, DatabaseCodegenModel.class);
    }

    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    public String getName() {
        return "kotlin-ObjectBox";
    }

    public String getHelp() {
        return "Generates a kotlin ObjectBox client.";
    }

    public void setDateLibrary(String library) {
        this.dateLibrary = library;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.invokerPackage = (String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE);
            additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
            packageName = invokerPackage + ".swagger.database";
            additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
            additionalProperties.put(CodegenConstants.MODEL_PACKAGE, packageName + ".models");
            modelPackage = packageName + ".models";
            additionalProperties.put(CodegenConstants.API_PACKAGE, packageName + ".api");
            apiPackage = packageName + ".api";
            importMapping.put("Infrastructure", packageName + ".infrastructure.*");
        } 

        if (!additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE)) {
            additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        }

        if (!additionalProperties.containsKey(CodegenConstants.API_PACKAGE)) {
            additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);
        }

        if (additionalProperties.containsKey(DATE_LIBRARY)) {
            setDateLibrary(additionalProperties.get(DATE_LIBRARY).toString());
        }

        if (DateLibrary.THREETENBP.value.equals(dateLibrary)) {
            additionalProperties.put(DateLibrary.THREETENBP.value, true);
            typeMapping.put("date", "LocalDate");
            typeMapping.put("DateTime", "LocalDateTime");
            importMapping.put("LocalDate", "org.threeten.bp.LocalDate");
            importMapping.put("LocalDateTime", "org.threeten.bp.LocalDateTime");
            defaultIncludes.add("org.threeten.bp.LocalDateTime");
        } else if (DateLibrary.STRING.value.equals(dateLibrary)) {
            typeMapping.put("date-time", "kotlin.String");
            typeMapping.put("date", "kotlin.String");
            typeMapping.put("Date", "kotlin.String");
            typeMapping.put("DateTime", "kotlin.String");
        } else if (DateLibrary.JAVA8.value.equals(dateLibrary)) {
            additionalProperties.put(DateLibrary.JAVA8.value, true);
        }

        final String infrastructureFolder = (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", "/");

        supportingFiles.add(new SupportingFile("infrastructure/EntityExtensions.kt.mustache", infrastructureFolder, "EntityExtensions.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/BoxCore.kt.mustache", infrastructureFolder, "BoxCore.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/UUIDEntity.kt.mustache", infrastructureFolder, "UUIDEntity.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/SoftDeletable.kt.mustache", infrastructureFolder, "SoftDeletable.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/StringListConverter.kt.mustache", infrastructureFolder, "StringListConverter.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ModelConversionExtensions.kt.mustache", infrastructureFolder, "ModelConversionExtensions.kt"));
    }

    /*
     * Derive invoker package name based on the input
     * e.g. foo.bar.model => foo.bar
     *
     * @param input API package/model name
     * @return Derived invoker package name based on API package/model name
     */
    private String deriveInvokerPackageName(String input) {
        String[] parts = input.split(Pattern.quote(".")); // Split on period.

        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String p : Arrays.copyOf(parts, parts.length-1)) {
            sb.append(delim).append(p);
            delim = ".";
        }
        return sb.toString();
    }

    // overridden to post-process any property properties
    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property){

        if (property == null || !(property instanceof DatabaseCodegenProperty)) {
            return;
        }
        DatabaseCodegenProperty typedProperty = (DatabaseCodegenProperty) property;
        typedProperty.processCustomProperties();
    }

    //Overridden to post-process any model properties.
    @Override
    public void postProcessModel(CodegenModel model){
        if (model == null || !(model instanceof DatabaseCodegenModel)) {
            return;
        }
        DatabaseCodegenModel typedModel = (DatabaseCodegenModel) model;
        typedModel.processCustomProperties(true);
    }

    // override with any special post-processing
    // Used here to not export non-database models
    @SuppressWarnings("static-method")
    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        
        Map<String, Object> allModels = new HashMap<String, Object>();
        
        for (Entry<String, Object> entry : objs.entrySet()) {
            String modelName = toModelName(entry.getKey());
            Map<String, Object> inner = (Map<String, Object>) entry.getValue();
            List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
            List<Map<String, Object>> newModels = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> mo : models) {
                try {
                    CodegenModel cm = (CodegenModel) mo.get("model");
                    if(cm instanceof DatabaseCodegenModel && ((DatabaseCodegenModel) cm).isDatabaseModel) {
                        System.out.println("Found database model for model: " + cm.name);
                        newModels.add(mo);
                    }
                }catch(java.lang.NullPointerException e) {
                    e.printStackTrace();
                }
            }

            if(newModels.size() > 0) {
                inner.put("models", newModels);
                allModels.put(entry.getKey(), inner);
            }
        }
        return allModels;
    }

    /**
     * Return the capitalized file name of the model
     *
     * @param name the model name
     * @return the file name of the model
     */
    @Override
    public String toModelFilename(String name) {
        return DefaultCodegen.camelize(name) + "Entity";
    }
}
