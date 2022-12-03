package com.guflimc.brick.libloader.spigot;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpigotBrickLibraryLoader {

    private final Logger logger;
    private final RepositorySystem repository;
    private final DefaultRepositorySystemSession session;

    private final List<RemoteRepository> repositories;

    public SpigotBrickLibraryLoader(Logger logger) {
        this.logger = logger;

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        this.repository = locator.getService(RepositorySystem.class);
        this.session = MavenRepositorySystemUtils.newSession();

        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
        session.setLocalRepositoryManager(repository.newLocalRepositoryManager(session, new LocalRepository("libraries")));
        session.setTransferListener(new AbstractTransferListener() {
            @Override
            public void transferStarted(@NotNull TransferEvent event) {
                logger.log(Level.INFO, "Downloading {0}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
            }
        });
        session.setReadOnly();

        this.repositories = repository.newResolutionRepositories(session, new ArrayList<>());
        repositories.add(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build());
    }

    public void addRepository(String id, String url) {
        repositories.add(new RemoteRepository.Builder(id, "default", url).build());
    }

    public void load(@NotNull String pluginName, @NotNull List<String> libraries) {
        if (libraries.isEmpty()) {
            return;
        }

        logger.log(Level.INFO, "[{0}] Loading {1} libraries... please wait",
                new Object[]{pluginName, libraries.size()});

        List<Dependency> dependencies = new ArrayList<>();
        for (String library : libraries) {
            Artifact artifact = new DefaultArtifact(library);
            Dependency dependency = new Dependency(artifact, null);
            dependencies.add(dependency);
        }

        try {
            repository.resolveDependencies(session, new DependencyRequest(new CollectRequest((Dependency) null, dependencies, repositories), null));
        } catch (DependencyResolutionException ex) {
            throw new RuntimeException("Error resolving libraries", ex);
        }
    }
}