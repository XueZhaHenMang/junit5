/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.TestDescriptor;

/**
 * @since 5.5
 */
class MethodOrderingVisitor extends AbstractOrderingVisitor {

	private final JupiterConfiguration configuration;

	MethodOrderingVisitor(JupiterConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void visit(TestDescriptor testDescriptor) {
		doWithMatchingDescriptor(ClassBasedTestDescriptor.class, testDescriptor,
			descriptor -> orderContainedMethods(descriptor, descriptor.getTestClass()),
			descriptor -> "Failed to order methods for " + descriptor.getTestClass());
	}

	@Override
	protected boolean shouldNonMatchingDescriptorsComeBeforeOrderedOnes() {
		// Non-matching descriptors can only contain @Nested test classes which should be
		// added after local test methods.
		return false;
	}

	/**
	 * @since 5.4
	 */
	private void orderContainedMethods(ClassBasedTestDescriptor classBasedTestDescriptor, Class<?> testClass) {
		findAnnotation(testClass, TestMethodOrder.class)//
				.map(TestMethodOrder::value)//
				.<MethodOrderer> map(ReflectionSupport::newInstance)//
				.map(Optional::of)//
				.orElseGet(configuration::getDefaultTestMethodOrderer)//
				.ifPresent(methodOrderer -> {

					Consumer<List<DefaultMethodDescriptor>> orderingAction = methodDescriptors -> methodOrderer.orderMethods(
						new DefaultMethodOrdererContext(testClass, methodDescriptors, this.configuration));

					MessageGenerator descriptorsAddedMessageGenerator = number -> String.format(
						"MethodOrderer [%s] added %s MethodDescriptor(s) for test class [%s] which will be ignored.",
						methodOrderer.getClass().getName(), number, testClass.getName());
					MessageGenerator descriptorsRemovedMessageGenerator = number -> String.format(
						"MethodOrderer [%s] removed %s MethodDescriptor(s) for test class [%s] which will be retained with arbitrary ordering.",
						methodOrderer.getClass().getName(), number, testClass.getName());

					DescriptorWrapperOrderer<DefaultMethodDescriptor> descriptorWrapperOrderer = new DescriptorWrapperOrderer<>(
						orderingAction, descriptorsAddedMessageGenerator, descriptorsRemovedMessageGenerator);

					orderChildrenTestDescriptors(classBasedTestDescriptor, //
						MethodBasedTestDescriptor.class, //
						DefaultMethodDescriptor::new, //
						descriptorWrapperOrderer);

					// Note: MethodOrderer#getDefaultExecutionMode() is guaranteed
					// to be invoked after MethodOrderer#orderMethods().
					methodOrderer.getDefaultExecutionMode()//
							.map(JupiterTestDescriptor::toExecutionMode)//
							.ifPresent(classBasedTestDescriptor::setDefaultChildExecutionMode);
				});
	}

}
