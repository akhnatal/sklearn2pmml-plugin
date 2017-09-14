/*
 * Copyright (c) 2017 Villu Ruusmann
 *
 * This file is part of SkLearn2PMML
 *
 * SkLearn2PMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SkLearn2PMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with SkLearn2PMML.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycompany;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.OpType;
import org.dmg.pmml.TypeDefinitionField;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.FeatureUtil;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.sklearn.SkLearnEncoder;
import sklearn.Transformer;

public class StringNormalizer extends Transformer {

	public StringNormalizer(String module, String name){
		super(module, name);
	}

	@Override
	public List<Feature> encodeFeatures(List<Feature> features, SkLearnEncoder encoder){
		String function = getFunction();
		Boolean trimBlanks = getTrimBlanks();

		if(function == null && !trimBlanks){
			return features;
		}

		List<Feature> result = new ArrayList<>();

		for(Feature feature : features){
			Expression expression = feature.ref();

			if(function != null){
				expression = PMMLUtil.createApply(function, expression);
			} // End if

			if(trimBlanks){
				expression = PMMLUtil.createApply("trimBlanks", expression);
			}

			TypeDefinitionField field = encoder.toCategorical(feature.getName(), Collections.<String>emptyList());

			// XXX: Should have been set by the previous transformer
			field.setDataType(DataType.STRING);

			DerivedField derivedField = encoder.createDerivedField(FeatureUtil.createName("normalize", feature), OpType.CATEGORICAL, DataType.STRING, expression);

			feature = new Feature(encoder, derivedField.getName(), derivedField.getDataType()){

				@Override
				public ContinuousFeature toContinuousFeature(){
					throw new UnsupportedOperationException();
				}
			};

			result.add(feature);
		}

		return result;
	}

	public String getFunction(){
		return (String)get("function");
	}

	public Boolean getTrimBlanks(){
		return (Boolean)get("trim_blanks");
	}
}