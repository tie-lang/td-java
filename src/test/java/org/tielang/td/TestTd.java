package org.tielang.td;

/** Unit tests for {@link Td} (plain main, no external dependencies). */
public final class TestTd {

    private static int failures = 0;

    private static void check(String name, boolean cond) {
        if (cond) {
            System.out.println("[PASS] " + name);
        } else {
            failures++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static boolean rejects(String source) {
        try {
            Td.parse(source);
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    public static void main(String[] args) {
        // Bare table, scalars, nesting, arrays.
        String sample = """
                type tie<data>
                [
                  name = "雨林" ,
                  count = 42,
                  ratio = 1.5,
                  active = true,
                  tags = ["a", "b"],
                  nested = [
                    x = 1,
                  ],
                ]
                """;
        TdTable table = Td.parse(sample);
        check("scalar str", "雨林".equals(table.get("name").asString()));
        check("scalar int", table.get("count").asInt() == 42);
        check("scalar float", table.get("ratio").asFloat() == 1.5);
        check("scalar bool", table.get("active").asBool());
        TdValue tags = table.get("tags");
        check("array elements", tags.asList().size() == 2 && "a".equals(tags.asList().get(0).asString()));
        TdValue nested = table.get("nested");
        check("nested get", nested instanceof TdTable nt && nt.get("x").asInt() == 1);

        // Optional table name + header stripping.
        TdTable named = Td.parse("type tie<data>\nworldgen = [\n  terrain = \"山地\",\n]\n");
        check("named table", "山地".equals(named.get("terrain").asString()));

        // Bare table without header.
        TdTable bare = Td.parse("[\n  x = 1,\n]\n");
        check("bare table", bare.get("x").asInt() == 1);

        // Escape round-trip through writer.
        TdTable built = TdTable.builder()
                .put("note", "line1\nline2\t\"q\"\\path")
                .put("v", TdValue.of(7L))
                .element(TdValue.of(1L))
                .build();
        String written = Td.write(built);
        TdTable reparsed = Td.parse(written);
        check("writer roundtrip str", "line1\nline2\t\"q\"\\path".equals(reparsed.get("note").asString()));
        check("writer roundtrip int", reparsed.get("v").asInt() == 7);
        check("writer roundtrip element", reparsed.elements().size() == 1 && reparsed.elements().get(0).asInt() == 1);

        // Malformed input must be rejected.
        check("reject unterminated", rejects("[\n  a = 1\n"));
        check("reject bad string", rejects("[\n  a = \"oops\n]\n"));
        check("reject trailing", rejects("[\n  a = 1,\n] extra\n"));

        if (failures > 0) {
            System.out.println(failures + " checks FAILED");
            System.exit(1);
        }
        System.out.println("all tests passed");
    }
}