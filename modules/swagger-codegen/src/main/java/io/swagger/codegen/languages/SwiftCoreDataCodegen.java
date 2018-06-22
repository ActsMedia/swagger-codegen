package io.swagger.codegen.languages;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import java.util.Map.Entry;

public class SwiftCoreDataCodegen extends AbstractSwiftCodegen implements CodegenConfig {

    @Override
    public String getName() {
        return "swiftCoreData";
    }

    @Override
    public String getHelp() {
        return "Generates a swift library for CoreData.";
    }

    /**
     * Constructor for the SwiftCoreDataCodegen language codegen module.
     */
    public SwiftCoreDataCodegen() {
        super();
        outputFolder = "generated-code" + File.separator + "swift";
        modelTemplateFiles.put("coreDataModel.mustache", ".swift");
        embeddedTemplateDir = templateDir = "swiftCoreData";
        apiPackage = File.separator + "APIs";
        modelPackage = File.separator + "Models";
    }

    @Override
    public void processOpts() {
        super.processOpts();
        final String infrastructureFolder = (sourceFolder  + File.separator + "infrastructure").replace(".", "/");
        
        supportingFiles.add(new SupportingFile("infrastructure/CDStack.mustache", infrastructureFolder, "CDStack.swift"));
        supportingFiles.add(new SupportingFile("infrastructure/CoreDataBuilders.mustache", infrastructureFolder, "CoreDataBuilders.swift"));
        supportingFiles.add(new SupportingFile("infrastructure/CoreDataChangeCheckers.mustache", infrastructureFolder, "CoreDataChangeCheckers.swift"));
        supportingFiles.add(new SupportingFile("infrastructure/CDStack.CoreDataCommonalities", infrastructureFolder, "CoreDataCommonalities.swift"));
        supportingFiles.add(new SupportingFile("infrastructure/CoreDataToSwaggerBuilders.mustache", infrastructureFolder, "CoreDataToSwaggerBuilders.swift"));
    }
    
   /**
     * Return the capitalized file name of the model
     *
     * @param name the model name
     * @return the file name of the model
     */
    @Override
    public String toModelFilename(String name) {
        return DefaultCodegen.camelize(name) + "CD";
    }

    // overridden to post-process any property properties
    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property){

        if (property == null || !(property instanceof DatabaseCodegenProperty)) {
            return;
        }
        DatabaseCodegenProperty typedProperty = (DatabaseCodegenProperty) property;
        typedProperty.processCustomProperties();
        
        super.postProcessModelProperty(model, property);

        // The default template code has the following logic for
        // assigning a type as Swift Optional:
        //
        // {{^unwrapRequired}}?{{/unwrapRequired}}
        // {{#unwrapRequired}}{{^required}}?{{/required}}{{/unwrapRequired}}
        //
        // which means:
        //
        // boolean isSwiftOptional = !unwrapRequired || (unwrapRequired && !property.required);
        //
        // We can drop the check for unwrapRequired in (unwrapRequired && !property.required)
        // due to short-circuit evaluation of the || operator.
        boolean isSwiftOptional = !unwrapRequired || !property.required;
        boolean isSwiftScalarType = property.isInteger || property.isLong || property.isFloat
                                    || property.isDouble || property.isBoolean;
        if (isSwiftOptional && isSwiftScalarType) {
            // Optional scalar types like Int?, Int64?, Float?, Double?, and Bool?
            // do not translate to Objective-C. So we want to flag those
            // properties in case we want to put special code in the templates
            // which provide Objective-C compatibility.
            property.vendorExtensions.put("x-swift-optional-scalar", true);
        }        
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
}
