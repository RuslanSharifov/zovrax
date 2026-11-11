/**
 * Available builds are derived exclusively from real release download files.
 * Never advertise a Minecraft version / loader combination that lacks a JAR.
 */
export function availableBuilds(mod) {
  return (mod.releases || []).flatMap((release, releaseIndex) =>
    (release.downloads || []).flatMap((file, fileIndex) => {
      if (!file?.url) return [];
      const versions = file.minecraftVersions || release.minecraftVersions || [];
      const loaders = file.loaders || release.loaders || [];
      return versions.flatMap(minecraft => loaders.map(loader => ({
        minecraft, loader, release: release.version,
        releaseIndex, fileIndex, file,
        prerelease: Boolean(release.prerelease),
      })));
    }));
}

export function matchingBuilds(builds, { minecraft, loader, release } = {}) {
  return builds.filter(build =>
    (!minecraft || build.minecraft === minecraft) &&
    (!loader || build.loader === loader) &&
    (!release || build.release === release));
}

export function distinctOptions(builds, key) {
  return [...new Set(builds.map(build => build[key]))];
}
