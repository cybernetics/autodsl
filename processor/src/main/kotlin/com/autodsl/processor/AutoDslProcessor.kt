/*
 * Copyright 2018 Juan Ignacio Saravia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.autodsl.processor

import com.autodsl.annotation.AutoDsl
import com.autodsl.processor.model.AutoDslClass
import com.autodsl.processor.model.generateClass
import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement


/**
 * AutoDsl Processor
 */
@AutoService(Processor::class)
@SupportedOptions(AutoDslProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AutoDslProcessor : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(AutoDsl::class.java).forEach { classElement ->
            if (classElement.kind != ElementKind.CLASS) {
                processingEnv.error(
                    classElement,
                    "Only classes can be annotated with %s.",
                    AutoDsl::class.java.simpleName
                )
                return true
            }
            classElement as TypeElement

            val modifiers = classElement.modifiers
            // check class is public and not abstract
            if (!modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.ABSTRACT)) {
                processingEnv.error(
                    classElement,
                    "The class %s is not public or is abstract.",
                    classElement.qualifiedName.toString()
                )
                return true
            }

            try {
                processingEnv.generateClass(AutoDslClass(classElement))
            } catch (pe: ProcessingException) {
                processingEnv.error(pe)
            } catch (e: Throwable) {
                processingEnv.error(
                    classElement,
                    "There was an error while processing your annotated classes. error = ${e.message.orEmpty()}"
                )
                return true
            }
        }
        return false // false=continue; true=exit process
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AutoDsl::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}