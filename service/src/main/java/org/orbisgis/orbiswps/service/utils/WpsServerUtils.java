package org.orbisgis.orbiswps.service.utils;

import net.opengis.ows._2.*;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.ObjectFactory;
import org.orbisgis.orbiswps.service.model.DataType;
import org.orbisgis.orbiswps.service.model.wpsmodel.Input;
import org.orbisgis.orbiswps.service.model.wpsmodel.Keyword;
import org.orbisgis.orbiswps.service.model.wpsmodel.Output;
import org.orbisgis.orbiswps.service.model.wpsmodel.WpsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility methods for the WpsService.
 *
 * @author Sylvain PALOMINOS
 */
public class WpsServerUtils {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsServerUtils.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(WpsServerUtils.class);

    /**
     * Creates a XMLGregorianCalendar object which represent the date of now + durationInMillis.
     * @param durationInMillis Duration in milliseconds to add to thenow date.
     * @return A XMLGregorianCalendar object which represent the date of now + durationInMillis.
     */
    public static XMLGregorianCalendar getXMLGregorianCalendar(long durationInMillis){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        XMLGregorianCalendar date = null;
        try {
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            date = datatypeFactory.newXMLGregorianCalendar(calendar);
            Duration duration = datatypeFactory.newDuration(durationInMillis);
            date.add(duration);
        } catch (DatatypeConfigurationException e) {
            LOGGER.error(I18N.tr("Unable to generate the XMLGregorianCalendar object.\nCause : {0}.", e.getMessage()));
        }
        return date;
    }

    public static ProcessOffering getProcessOfferingFromModel(WpsModel model){
        ObjectFactory factory = new ObjectFactory();
        ProcessDescriptionType processDescriptionType = new ProcessDescriptionType();

        CodeType idCodeType = new CodeType();
        idCodeType.setValue(model.getIdentifier());
        processDescriptionType.setIdentifier(idCodeType);

        LanguageStringType title = new LanguageStringType();
        title.setValue(model.getTitle());
        processDescriptionType.getTitle().add(title);

        if(model.getAbstract() != null) {
            LanguageStringType abstr = new LanguageStringType();
            abstr.setValue(model.getAbstract());
            processDescriptionType.getAbstract().add(abstr);
        }

        if(model.getKeywords() != null) {
            for (Keyword key : model.getKeywords().getKeyword()) {
                LanguageStringType keyword = new LanguageStringType();
                keyword.setValue(key.getValue());
                keyword.setLang(key.getLang());
                KeywordsType keywordsType = new KeywordsType();
                keywordsType.getKeyword().add(keyword);
                processDescriptionType.getKeywords().add(keywordsType);
            }
        }

        for(Input input : model.getInputs().getInput()){
            InputDescriptionType inputDescriptionType = new InputDescriptionType();

            CodeType processId = new CodeType();
            processId.setValue(input.getIdentifier());
            inputDescriptionType.setIdentifier(processId);

            LiteralDataType.LiteralDataDomain domain = new LiteralDataType.LiteralDataDomain();
            domain.setDefault(true);
            domain.setAnyValue(new AnyValue());
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue(DataType.STRING.name());
            domain.setDataType(domainMetadataType);
            LiteralDataType literalDataType = new LiteralDataType();
            literalDataType.getLiteralDataDomain().add(domain);
            inputDescriptionType.setDataDescription(factory.createLiteralData(literalDataType));

            LanguageStringType inTitle = new LanguageStringType();
            inTitle.setValue(input.getTitle());
            inputDescriptionType.getTitle().add(inTitle);

            if(input.getAbstract() != null) {
                LanguageStringType inAbstract = new LanguageStringType();
                inAbstract.setValue(input.getAbstract());
                inputDescriptionType.getAbstract().add(inAbstract);
            }

            processDescriptionType.getInput().add(inputDescriptionType);
        }

        for(Output output : model.getOutputs().getOutput()){
            OutputDescriptionType outputDescriptionType = new OutputDescriptionType();

            CodeType processId = new CodeType();
            processId.setValue(output.getIdentifier());
            outputDescriptionType.setIdentifier(processId);

            LiteralDataType.LiteralDataDomain domain = new LiteralDataType.LiteralDataDomain();
            domain.setDefault(true);
            domain.setAnyValue(new AnyValue());
            LiteralDataType literalDataType = new LiteralDataType();
            literalDataType.getLiteralDataDomain().add(domain);
            outputDescriptionType.setDataDescription(factory.createLiteralData(literalDataType));

            LanguageStringType outTitle = new LanguageStringType();
            outTitle.setValue(output.getTitle());
            outputDescriptionType.getTitle().add(outTitle);

            if(output.getAbstract() != null) {
                LanguageStringType outAbstract = new LanguageStringType();
                outAbstract.setValue(output.getAbstract());
                outputDescriptionType.getAbstract().add(outAbstract);
            }

            processDescriptionType.getOutput().add(outputDescriptionType);
        }

        ProcessOffering processOffering = new ProcessOffering();
        processOffering.setProcess(processDescriptionType);
        return processOffering;
    }
}
