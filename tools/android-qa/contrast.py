"""Check opaque text/background pairs from the current application theme."""
import json
import re
import qa

def luminance(color):
    values = [int(color[index:index+2], 16) / 255 for index in (0, 2, 4)]
    linear = [value / 12.92 if value <= .04045 else ((value + .055) / 1.055) ** 2.4 for value in values]
    return sum(value * weight for value, weight in zip(linear, [.2126, .7152, .0722]))

theme = next((qa.ROOT / 'apps/mobile/android/app/src/main/java').rglob('*PremiumTheme.kt'))
colors = dict(re.findall(r'(\w+) = Color\(0xFF([0-9A-F]{6})\)', theme.read_text(encoding='utf-8')))
pairs = []
for foreground, background in [('onPrimary', 'primary'), ('onPrimaryContainer', 'primaryContainer'), ('onBackground', 'background'), ('onSurface', 'surface'), ('onSurfaceVariant', 'surfaceVariant')]:
    low, high = sorted([luminance(colors[foreground]), luminance(colors[background])])
    ratio = (high + .05) / (low + .05)
    assert ratio >= 4.5, (foreground, background, ratio)
    pairs.append(dict(foreground=foreground, background=background, ratio=round(ratio, 2), threshold=4.5))
output = qa.ROOT / 'reports' / qa.RUN
output.mkdir(parents=True, exist_ok=True)
(output / 'contrast.json').write_text(json.dumps(dict(status='passed', scope='opaque theme text pairs; excludes SDK ad creative, animation and disabled controls', pairs=pairs), indent=2), encoding='utf-8')
print(qa.SLUG, 'CONTRAST PASS', flush=True)
