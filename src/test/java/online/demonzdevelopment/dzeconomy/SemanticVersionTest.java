package online.demonzdevelopment.dzeconomy.update;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for SemanticVersion – version comparison and parsing logic.
 * Pure unit tests, no Bukkit dependency needed.
 */
class SemanticVersionTest {

    // ━━ Parsing Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("Parse standard version string")
    void testParseStandardVersion() {
        SemanticVersion version = new SemanticVersion("2.0.0");

        assertThat(version.toString()).isEqualTo("2.0.0");
    }

    @Test
    @DisplayName("Parse version with pre-release suffix")
    void testParsePreReleaseVersion() {
        SemanticVersion version = new SemanticVersion("2.0.0-beta");

        assertThat(version.toString()).isEqualTo("2.0.0-beta");
    }

    @Test
    @DisplayName("Parse version with complex pre-release suffix")
    void testParseComplexPreRelease() {
        SemanticVersion version = new SemanticVersion("1.5.3-rc.1");

        assertThat(version.toString()).isEqualTo("1.5.3-rc.1");
    }

    @Test
    @DisplayName("Invalid version throws IllegalArgumentException")
    void testInvalidVersionThrowsException() {
        assertThatThrownBy(() -> new SemanticVersion("not-a-version"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid semantic version");

        assertThatThrownBy(() -> new SemanticVersion("2.0"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new SemanticVersion("2"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new SemanticVersion(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new SemanticVersion("abc.def.ghi"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ━━ Comparison Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("2.0.0 > 1.9.9 (major version difference)")
    void testMajorVersionComparison() {
        SemanticVersion v200 = new SemanticVersion("2.0.0");
        SemanticVersion v199 = new SemanticVersion("1.9.9");

        assertThat(v200.isNewerThan(v199)).isTrue();
        assertThat(v199.isNewerThan(v200)).isFalse();
    }

    @Test
    @DisplayName("2.1.0 > 2.0.9 (minor version difference)")
    void testMinorVersionComparison() {
        SemanticVersion v210 = new SemanticVersion("2.1.0");
        SemanticVersion v209 = new SemanticVersion("2.0.9");

        assertThat(v210.isNewerThan(v209)).isTrue();
        assertThat(v209.isNewerThan(v210)).isFalse();
    }

    @Test
    @DisplayName("2.0.1 > 2.0.0 (patch version difference)")
    void testPatchVersionComparison() {
        SemanticVersion v201 = new SemanticVersion("2.0.1");
        SemanticVersion v200 = new SemanticVersion("2.0.0");

        assertThat(v201.isNewerThan(v200)).isTrue();
        assertThat(v200.isNewerThan(v201)).isFalse();
    }

    @Test
    @DisplayName("2.0.0 > 2.0.0-beta (release > pre-release)")
    void testReleaseNewerThanPreRelease() {
        SemanticVersion release = new SemanticVersion("2.0.0");
        SemanticVersion beta = new SemanticVersion("2.0.0-beta");

        assertThat(release.isNewerThan(beta)).isTrue();
        assertThat(beta.isNewerThan(release)).isFalse();
    }

    @Test
    @DisplayName("2.0.0-alpha < 2.0.0-beta (pre-release alphabetical)")
    void testPreReleaseAlphabetical() {
        SemanticVersion alpha = new SemanticVersion("2.0.0-alpha");
        SemanticVersion beta = new SemanticVersion("2.0.0-beta");

        // "alpha" < "beta" lexicographically
        assertThat(beta.isNewerThan(alpha)).isTrue();
        assertThat(alpha.isNewerThan(beta)).isFalse();
    }

    @Test
    @DisplayName("Equal versions compare as equal")
    void testEqualVersions() {
        SemanticVersion v1 = new SemanticVersion("2.0.0");
        SemanticVersion v2 = new SemanticVersion("2.0.0");

        assertThat(v1.isNewerThan(v2)).isFalse();
        assertThat(v2.isNewerThan(v1)).isFalse();
        assertThat(v1.compareTo(v2)).isEqualTo(0);
    }

    @Test
    @DisplayName("Equal pre-release versions compare as equal")
    void testEqualPreReleaseVersions() {
        SemanticVersion v1 = new SemanticVersion("2.0.0-beta");
        SemanticVersion v2 = new SemanticVersion("2.0.0-beta");

        assertThat(v1.compareTo(v2)).isEqualTo(0);
        assertThat(v1.isNewerThan(v2)).isFalse();
    }

    // ━━ Comparable Contract Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("compareTo is consistent with equals")
    void testCompareToConsistency() {
        SemanticVersion v1 = new SemanticVersion("1.0.0");
        SemanticVersion v2 = new SemanticVersion("1.0.0");

        // If compareTo returns 0, the versions should be "equal" in ordering
        assertThat(v1.compareTo(v2)).isEqualTo(0);
    }

    @Test
    @DisplayName("compareTo satisfies signum symmetry: sgn(x.compareTo(y)) == -sgn(y.compareTo(x))")
    void testCompareToSymmetry() {
        SemanticVersion higher = new SemanticVersion("2.0.0");
        SemanticVersion lower = new SemanticVersion("1.0.0");

        int forward = higher.compareTo(lower);
        int reverse = lower.compareTo(higher);

        assertThat(Integer.signum(forward)).isEqualTo(-Integer.signum(reverse));
    }

    @Test
    @DisplayName("compareTo is transitive: if A > B and B > C, then A > C")
    void testCompareToTransitivity() {
        SemanticVersion a = new SemanticVersion("3.0.0");
        SemanticVersion b = new SemanticVersion("2.0.0");
        SemanticVersion c = new SemanticVersion("1.0.0");

        assertThat(a.compareTo(b)).isGreaterThan(0);
        assertThat(b.compareTo(c)).isGreaterThan(0);
        assertThat(a.compareTo(c)).isGreaterThan(0);
    }

    // ━━ Edge Case Tests ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    @DisplayName("Version with leading/trailing whitespace is trimmed")
    void testVersionWithWhitespace() {
        SemanticVersion version = new SemanticVersion("  2.0.0  ");

        assertThat(version.toString()).isEqualTo("2.0.0");
    }

    @Test
    @DisplayName("Large version numbers compare correctly")
    void testLargeVersionNumbers() {
        SemanticVersion v1 = new SemanticVersion("99.99.99");
        SemanticVersion v2 = new SemanticVersion("100.0.0");

        assertThat(v2.isNewerThan(v1)).isTrue();
    }

    @Test
    @DisplayName("1.0.0-release vs 1.0.0 (release without suffix is newer)")
    void testReleaseVsExplicitReleaseSuffix() {
        SemanticVersion noSuffix = new SemanticVersion("1.0.0");
        SemanticVersion withSuffix = new SemanticVersion("1.0.0-release");

        // No suffix (release) > with suffix (pre-release-like)
        assertThat(noSuffix.isNewerThan(withSuffix)).isTrue();
    }
}
