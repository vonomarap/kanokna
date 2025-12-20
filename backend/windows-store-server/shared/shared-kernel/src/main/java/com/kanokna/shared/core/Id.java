package com.kanokna.shared.core;

import java.util.Objects;
import java.util.UUID;

public final class Id {
    private final String value;

    private Id(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID must be non-empty");
        }
        this.value = value;
    }

    /*<FUNCTION_CONTRACT
        id="id.of"
        module="mod.shared.kernel"
        SPECIFICATION="RequirementsAnalysis.xml#ORDER.IDEMPOTENCY"
        LINKS="MODULE_CONTRACT#mod.shared.kernel">
      <ROLE_IN_MODULE>
        Create a stable identifier from a provided string while enforcing non-blank semantics for entity and event IDs.
      </ROLE_IN_MODULE>
      <SIGNATURE>
        <INPUT>
          - value:String non-null, non-blank identifier (UUID or business key).
        </INPUT>
        <OUTPUT>
          - Immutable Id wrapper exposing value().
        </OUTPUT>
        <SIDE_EFFECTS>
          - None.
        </SIDE_EFFECTS>
      </SIGNATURE>
      <PRECONDITIONS>
        - value is not null or blank.
      </PRECONDITIONS>
      <POSTCONDITIONS>
        - Returned Id.value() equals the provided string.
      </POSTCONDITIONS>
      <INVARIANTS>
        - Id is immutable; equals/hashCode based solely on value.
      </INVARIANTS>
      <ERROR_HANDLING>
        - IllegalArgumentException when value is null or blank.
      </ERROR_HANDLING>
      <LOGGING>
        - None.
      </LOGGING>
      <TEST_CASES>
        <HAPPY_PATH>
          - value=\"123\" -> Id.value()==\"123\".
        </HAPPY_PATH>
        <EDGE_CASES>
          - value=\"\" or whitespace -> IllegalArgumentException.
          - value=null -> IllegalArgumentException.
        </EDGE_CASES>
        <SECURITY_CASES>
          - Not applicable.
        </SECURITY_CASES>
      </TEST_CASES>
    </FUNCTION_CONTRACT>
    */
    public static Id of(String value) {
        return new Id(value);
    }

    /*<FUNCTION_CONTRACT
        id="id.random"
        module="mod.shared.kernel"
        SPECIFICATION="RequirementsAnalysis.xml#ORDER.IDEMPOTENCY"
        LINKS="MODULE_CONTRACT#mod.shared.kernel">
      <ROLE_IN_MODULE>
        Generate a new random UUID-based identifier for entities or events.
      </ROLE_IN_MODULE>
      <SIGNATURE>
        <INPUT>
          - None.
        </INPUT>
        <OUTPUT>
          - Id containing UUID string (RFC 4122) in canonical form.
        </OUTPUT>
        <SIDE_EFFECTS>
          - None; relies on UUID randomness.
        </SIDE_EFFECTS>
      </SIGNATURE>
      <PRECONDITIONS>
        - None.
      </PRECONDITIONS>
      <POSTCONDITIONS>
        - Returned Id is non-null, non-blank, and unique with high probability.
      </POSTCONDITIONS>
      <INVARIANTS>
        - Id remains immutable after creation.
      </INVARIANTS>
      <ERROR_HANDLING>
        - None expected.
      </ERROR_HANDLING>
      <LOGGING>
        - None.
      </LOGGING>
      <TEST_CASES>
        <HAPPY_PATH>
          - random() returns Id whose value matches UUID pattern.
        </HAPPY_PATH>
        <EDGE_CASES>
          - Multiple calls produce distinct values (statistical uniqueness).
        </EDGE_CASES>
        <SECURITY_CASES>
          - Not applicable.
        </SECURITY_CASES>
      </TEST_CASES>
    </FUNCTION_CONTRACT>
    */
    public static Id random() {
        return new Id(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) 
            return true;
        if(!(o instanceof Id that)) 
            return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override 
    public String toString() {
        return value;
    }
}
