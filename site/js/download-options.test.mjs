import test from 'node:test';
import assert from 'node:assert/strict';
import { availableBuilds, matchingBuilds, distinctOptions } from './download-options.js';

const mod = { releases: [
  { version: '0.2.2', minecraftVersions: ['1.21.10'], loaders: ['Fabric'],
    downloads: [{ url: 'data/a.jar', name: 'a.jar', minecraftVersions: ['1.21.10'], loaders: ['Fabric'] }] },
  { version: '0.2.1', minecraftVersions: ['1.21.10'], loaders: ['Fabric'],
    downloads: [{ url: 'data/b.jar', name: 'b.jar' }] },
  { version: '1.0.0', minecraftVersions: ['1.20.1'], loaders: ['Forge'],
    downloads: [{ url: 'data/c.jar', name: 'c.jar' }] },
  { version: '1.1.0', minecraftVersions: ['1.21.10'], loaders: ['NeoForge'],
    downloads: [] },
] };

test('picker only offers Minecraft / loader options that have a real file', () => {
  const builds = availableBuilds(mod);
  assert.deepEqual(distinctOptions(builds, 'minecraft'), ['1.21.10', '1.20.1']);
  assert.deepEqual(distinctOptions(matchingBuilds(builds, { minecraft: '1.21.10' }), 'loader'), ['Fabric']);
  assert.equal(matchingBuilds(builds, { minecraft: '1.21.10', loader: 'Forge' }).length, 0);
});

test('mod release filters the exact downloadable file', () => {
  const builds = availableBuilds(mod);
  assert.deepEqual(matchingBuilds(builds, { minecraft: '1.21.10', loader: 'Fabric', release: '0.2.1' })
    .map(x => x.file.name), ['b.jar']);
});

test('planned or premium-only mod with no files has no download choices', () => {
  assert.deepEqual(availableBuilds({ releases: [] }), []);
  assert.deepEqual(availableBuilds({ releases: [{ version: '1', downloads: [] }] }), []);
});
