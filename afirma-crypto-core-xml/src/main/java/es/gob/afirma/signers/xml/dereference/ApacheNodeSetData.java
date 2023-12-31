/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 */
/*
 * $Id: ApacheNodeSetData.java 1203890 2011-11-18 22:47:56Z mullan $
 */

package es.gob.afirma.signers.xml.dereference;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.crypto.NodeSetData;

import org.w3c.dom.Node;

/** Implementaci&oacute;n Apache del <code>NodeSetData</code>. */
public final class ApacheNodeSetData implements NodeSetData/*<Node>*/ {

    private final XMLSignatureInput xi;

    ApacheNodeSetData(final XMLSignatureInput xi) {
        this.xi = xi;
    }

    @Override
	public Iterator<Node> iterator() {
        // If nodefilters are set, must execute them first to create node-set
        if (this.xi.getNodeFilters() != null && !this.xi.getNodeFilters().isEmpty()) {
            return Collections.unmodifiableSet
                (getNodeSet(this.xi.getNodeFilters())).iterator();
        }
        try {
            return Collections.unmodifiableSet(this.xi.getNodeSet()).iterator();
        }
        catch (final Exception e) {
            // should not occur
            throw new RuntimeException("unrecoverable error retrieving nodeset: " + e, e); //$NON-NLS-1$
        }
    }

    private Set<Node> getNodeSet(final List<NodeFilter> nodeFilters) {
        if (this.xi.isNeedsToBeExpanded()) {
            XMLUtils.circumventBug2650(
        		XMLUtils.getOwnerDocument(this.xi.getSubNode())
    		);
        }

        final Set<Node> inputSet = new LinkedHashSet<>();
        XMLUtils.getSet(
    		this.xi.getSubNode(),
    		inputSet,
            null,
            !this.xi.isExcludeComments()
        );
        final Set<Node> nodeSet = new LinkedHashSet<>();
        for (final Node currentNode : inputSet) {
            final Iterator<NodeFilter> it = nodeFilters.iterator();
            boolean skipNode = false;
            while (it.hasNext() && !skipNode) {
                final NodeFilter nf = it.next();
                if (nf.isNodeInclude(currentNode) != 1) {
                    skipNode = true;
                }
            }
            if (!skipNode) {
                nodeSet.add(currentNode);
            }
        }
        return nodeSet;
    }

}