/*
 * OrbisWPS contains a set of libraries to build a Web Processing Service (WPS)
 * compliant with the 2.0 specification.
 *
 * OrbisWPS is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OrbisWPS is distributed under GPL 3 license.
 *
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OrbisWPS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisWPS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisWPS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbiswps.service.process;

import net.opengis.ows._2.KeywordsType;
import net.opengis.ows._2.LanguageStringType;
import net.opengis.wps._2_0.DescriptionType;
import net.opengis.wps._2_0.InputDescriptionType;
import net.opengis.wps._2_0.OutputDescriptionType;
import net.opengis.wps._2_0.ProcessDescriptionType;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.orbisgis.orbiswps.serviceapi.model.TranslatableComplexData;
import org.xnap.commons.i18n.I18n;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class contains methods which does the translation of a process.
 *
 * @author Sylvain PALOMINOS
 */
public class ProcessTranslator {


    /**
     * Return the process with the given language translation.
     * If the asked translation doesn't exists, use the english one. If it doesn't exists too, uses one of the others.
     * @param languages Languages asked.
     * @return The traduced process.
     */
    public static ProcessDescriptionType getTranslatedProcess(
            ProcessIdentifier pi, List<String> languages){
        I18n i18n = pi.getI18n();
        ProcessDescriptionType process = pi.getProcessDescriptionType();
        ProcessDescriptionType translatedProcess = new ProcessDescriptionType();
        translatedProcess.setLang(languages.get(0));
        List<InputDescriptionType> inputList = new ArrayList<>();
        for(InputDescriptionType input : process.getInput()){
            InputDescriptionType translatedInput = new InputDescriptionType();
            JAXBElement jaxbElement = input.getDataDescription();
            if(jaxbElement.getValue() instanceof TranslatableComplexData){
                TranslatableComplexData translatableComplexData = (TranslatableComplexData)jaxbElement.getValue();
                jaxbElement.setValue(translatableComplexData.getTranslatedData(i18n, languages));
            }
            translatedInput.setDataDescription(jaxbElement);
            translatedInput.setMaxOccurs(input.getMaxOccurs());
            translatedInput.setMinOccurs(input.getMinOccurs());
            translateDescriptionType(translatedInput, input, languages, i18n);
            inputList.add(translatedInput);
        }
        translatedProcess.getInput().clear();
        translatedProcess.getInput().addAll(inputList);
        List<OutputDescriptionType> outputList = new ArrayList<>();
        for(OutputDescriptionType output : process.getOutput()){
            OutputDescriptionType translatedOutput = new OutputDescriptionType();
            JAXBElement jaxbElement = output.getDataDescription();
            if(jaxbElement.getValue() instanceof TranslatableComplexData){
                TranslatableComplexData translatableComplexData = (TranslatableComplexData)jaxbElement.getValue();
                jaxbElement.setValue(translatableComplexData.getTranslatedData(i18n, languages));
            }
            translatedOutput.setDataDescription(jaxbElement);
            translateDescriptionType(translatedOutput, output, languages, i18n);
            outputList.add(translatedOutput);
        }
        translatedProcess.getOutput().clear();
        translatedProcess.getOutput().addAll(outputList);
        translateDescriptionType(translatedProcess, process, languages, pi.getI18n());
        return translatedProcess;
    }

    /**
     * Sets the given translatedDescriptionType with the traduced elements of the source descriptionType.
     * If the asked translation doesn't exists, use the english one. If it doesn't exists too, uses one of the others.
     *
     * @param translatedDescriptionType Translated DescriptionType.
     * @param descriptionType Source DescriptionType.
     * @param languages Languages asked.
     */
    public static void translateDescriptionType(DescriptionType translatedDescriptionType,
                                                DescriptionType descriptionType,
                                                List<String> languages,
                                                I18n i18n){
        translatedDescriptionType.setIdentifier(descriptionType.getIdentifier());
        translatedDescriptionType.getMetadata().clear();
        translatedDescriptionType.getMetadata().addAll(descriptionType.getMetadata());
        //Find the good abstract
        List<LanguageStringType> abstrList = new ArrayList<>();
        for (LanguageStringType abstr : descriptionType.getAbstract()) {
            for(String lang : languages) {
                i18n.setLocale(Locale.forLanguageTag(lang.substring(0, 2)));
                LanguageStringType translatedAbstract = new LanguageStringType();
                translatedAbstract.setLang(lang);
                translatedAbstract.setValue(i18n.tr(abstr.getValue()));
                abstrList.add(translatedAbstract);
            }
        }
        translatedDescriptionType.getAbstract().clear();
        translatedDescriptionType.getAbstract().addAll(abstrList);
        //Find the good title
        List<LanguageStringType> titleList = new ArrayList<>();
        for (LanguageStringType title : descriptionType.getTitle()) {
            for(String lang : languages) {
                i18n.setLocale(Locale.forLanguageTag(lang.substring(0, 2)));
                LanguageStringType translatedTitle = new LanguageStringType();
                translatedTitle.setLang(lang);
                translatedTitle.setValue(i18n.tr(title.getValue()));
                titleList.add(translatedTitle);
            }
        }
        translatedDescriptionType.getTitle().clear();
        translatedDescriptionType.getTitle().addAll(titleList);
        //Find the good keywords
        List<KeywordsType> keywordsList = new ArrayList<>();
        for(KeywordsType keywords : descriptionType.getKeywords()) {
            KeywordsType translatedKeywords = new KeywordsType();
            for (LanguageStringType keyword : keywords.getKeyword()) {
                List<LanguageStringType> keywordList = new ArrayList<>();
                for(String lang : languages) {
                    i18n.setLocale(Locale.forLanguageTag(lang.substring(0, 2)));
                    LanguageStringType translatedKeyword = new LanguageStringType();
                    translatedKeyword.setLang(lang);
                    translatedKeyword.setValue(i18n.tr(keyword.getValue()));
                    keywordList.add(translatedKeyword);
                }
                translatedKeywords.getKeyword().clear();
                translatedKeywords.getKeyword().addAll(keywordList);
            }
            keywordsList.add(translatedKeywords);
        }
        translatedDescriptionType.getKeywords().clear();
        translatedDescriptionType.getKeywords().addAll(keywordsList);
        translatedDescriptionType.setIdentifier(descriptionType.getIdentifier());
    }
}
