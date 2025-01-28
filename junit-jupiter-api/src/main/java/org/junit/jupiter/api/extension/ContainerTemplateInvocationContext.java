/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * @since 5.13
 */
@API(status = EXPERIMENTAL, since = "5.13")
public interface ContainerTemplateInvocationContext {

	/**
	 * Get the display name for this invocation.
	 *
	 * <p>The supplied {@code invocationIndex} is incremented by the framework
	 * with each container invocation. Thus, in the case of multiple active
	 * {@linkplain ContainerTemplateInvocationContextProvider providers}, only
	 * the first active provider receives indices starting with {@code 1}.
	 *
	 * <p>The default implementation returns the supplied {@code invocationIndex}
	 * wrapped in brackets &mdash; for example, {@code [1]}, {@code [42]}, etc.
	 *
	 * @param invocationIndex the index of this invocation (1-based).
	 * @return the display name for this invocation; never {@code null} or blank
	 */
	default String getDisplayName(int invocationIndex) {
		return "[" + invocationIndex + "]";
	}

}
