/*
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
package org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasTraversalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.RangeGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.CountGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public class HalfStepTraversalStrategy extends AbstractTraversalStrategy implements TraversalStrategy {

    private static final HalfStepTraversalStrategy INSTANCE = new HalfStepTraversalStrategy();

    private HalfStepTraversalStrategy() {
    }

    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        final int size = traversal.getSteps().size() - 1;
        Step prev = null;
        for (int i = 0; i <= size; i++) {
            final Step curr = traversal.getSteps().get(i);
            VertexStep stepToReplace = null;
            if (i == size && curr instanceof VertexStep && Vertex.class.equals(((VertexStep) curr).getReturnClass())) {
                final TraversalParent parent = curr.getTraversal().getParent();
                if (parent instanceof HasTraversalStep) {
                    stepToReplace = (VertexStep) curr;
                }
            } else if (prev instanceof VertexStep && Vertex.class.equals(((VertexStep) prev).getReturnClass())) {
                if (curr instanceof CountGlobalStep) {
                    stepToReplace = (VertexStep) prev;
                }
            }
            if (stepToReplace != null) {
                TraversalHelper.replaceStep(stepToReplace, new VertexStep(traversal, Edge.class, stepToReplace.getDirection(),
                        stepToReplace.getEdgeLabels()), traversal);
            }
            if (!(curr instanceof RangeGlobalStep)) {
                prev = curr;
            }
        }
    }

    public static HalfStepTraversalStrategy instance() {
        return INSTANCE;
    }
}
