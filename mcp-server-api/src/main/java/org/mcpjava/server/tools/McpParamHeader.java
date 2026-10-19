/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mcpjava.server.tools;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a parameter of a {@link Tool} method whose value clients must mirror into an HTTP
 * request header.
 * <p>
 * The designation is published in the tool's JSON Schema as the {@code x-mcp-header} keyword on
 * the parameter's property schema. When a client calls the tool over the Streamable HTTP
 * transport, it sends the argument value in the {@code Mcp-Param-<value>} header in addition to
 * the request body, so that intermediaries such as gateways and load balancers can route or
 * inspect the call without parsing the body.
 * <p>
 * This annotation only affects the Streamable HTTP transport, and only when the client and server
 * have negotiated a protocol version that supports it. It is kept separate from {@link ToolArg}
 * for that reason, and the two sit on the same parameter. {@link ToolArg} describes the property
 * — its name, its description, whether it is required — and this annotation adds the
 * {@code x-mcp-header} keyword to that same property. Neither is restated here, so that each
 * field of a property's schema has exactly one source.
 *
 * <pre>{@code
 * @Tool(name = "execute_sql", description = "Execute SQL on Google Cloud Spanner")
 * List<Row> executeSql(
 *         @ToolArg(description = "The region to execute the query in")
 *         @McpParamHeader("Region") String region,
 *         @ToolArg(description = "The SQL query to execute") String query) {
 *     ...
 * }
 * }</pre>
 *
 * is published as:
 *
 * <pre>{@code
 * {
 *   "name": "execute_sql",
 *   "description": "Execute SQL on Google Cloud Spanner",
 *   "inputSchema": {
 *     "type": "object",
 *     "properties": {
 *       "region": {
 *         "type": "string",
 *         "description": "The region to execute the query in",
 *         "x-mcp-header": "Region"
 *       },
 *       "query": {
 *         "type": "string",
 *         "description": "The SQL query to execute"
 *       }
 *     },
 *     "required": ["region", "query"]
 *   }
 * }
 * }</pre>
 *
 * Both parameters appear in {@code required} because {@link ToolArg#required()} defaults to
 * {@code true}, and {@code region} carries its description and its header designation from the
 * two annotations it is given.
 *
 * <h2>Constraints</h2>
 * The specification places the following constraints on the designation:
 * <ul>
 * <li>{@link #value()} must not be empty;
 * <li>{@link #value()} must contain only ASCII characters, excluding space and {@code :};
 * <li>{@link #value()} must be unique, ignoring case, among the parameters of a single tool;
 * <li>the annotated parameter must be of a primitive JSON Schema type: {@code integer},
 * {@code string} or {@code boolean}. {@code number} is not permitted, so parameters of type
 * {@code float}, {@code double} or {@link java.math.BigDecimal}, for example, cannot be
 * designated.
 * </ul>
 * <p>
 * Clients must exclude a tool whose designation violates these constraints from the tools they
 * make available, rather than report an error. Implementations should therefore validate the
 * designation when the tool is registered, so that a mistake is reported to the developer instead
 * of the tool silently disappearing from clients.
 * <p>
 * The header value travels in plain view of every intermediary, which may log it. Avoid
 * designating parameters that carry sensitive data, such as passwords, API keys, tokens or
 * personal information.
 *
 * @see ToolArg
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/server/tools#custom-headers">MCP
 * Specification - Tools - Custom Headers</a>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpParamHeader {

    /**
     * The name appended to {@code Mcp-Param-} to make the request header name.
     * <p>
     * This value is used for the {@code x-mcp-header} property in the parameter schema, which instructs clients to send the argument
     * value in an {@code Mcp-Param-<value>} request header.
     * <p>
     * This value must be:
     * <ul>
     * <li>A valid HTTP header name (see <a href="https://datatracker.ietf.org/doc/html/rfc9110#section-5.1">RFC 9110 Section 5.1</a>)
     * <li>Case-insensitively unique among the parameters of the tool
     * </ul>
     *
     * @return the header name suffix
     */
    String value();
}
