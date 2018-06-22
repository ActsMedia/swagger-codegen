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

public class SwiftClientGenerator extends AbstractSwiftCodegen implements CodegenConfig {
    protected String projectName = "SwaggerClient";


    @Override
    public String getName() {
        return "swift-client";
    }

    @Override
    public String getHelp() {
        return "Generates a swift client.";
    }

    /**
     * Constructor for the SwiftClientGenerator language codegen module.
     */
    public SwiftClientGenerator() {
        super();
        sourceFolder = "Swagger";
        outputFolder = "generated-code" + File.separator + "swift";
        modelTemplateFiles.put("model.mustache", ".swift");
        apiTemplateFiles.put("api.mustache", ".swift");
        embeddedTemplateDir = templateDir = "swift-client";
        apiPackage = File.separator + "APIs";
        modelPackage = File.separator + "Models";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        String infrastructureFolderPath = sourceFolder+ File.separator + "Infrastructure";
        supportingFiles.add(new SupportingFile("Extensions.mustache", infrastructureFolderPath, "Extensions.swift"));
        supportingFiles.add(new SupportingFile("NetworkCall.mustache", infrastructureFolderPath, "NetworkCall.swift"));
        supportingFiles.add(new SupportingFile("NetworkHelper.mustache", infrastructureFolderPath, "NetworkHelper.swift"));
        supportingFiles.add(new SupportingFile("NetworkRequestor.mustache", infrastructureFolderPath, "NetworkRequestor.swift"));
        supportingFiles.add(new SupportingFile("OAuthAPI.mustache", infrastructureFolderPath, "OAuthAPI.swift"));
        supportingFiles.add(new SupportingFile("TokenManager.mustache", infrastructureFolderPath, "TokenManager.swift"));
    }
}
