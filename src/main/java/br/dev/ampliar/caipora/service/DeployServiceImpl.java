package br.dev.ampliar.caipora.service;

import br.dev.ampliar.caipora.domain.Deploy;
import br.dev.ampliar.caipora.model.DeployDTO;
import br.dev.ampliar.caipora.repos.DeployRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.repos.UserRepository;
import br.dev.ampliar.caipora.repos.VersionRepository;
import br.dev.ampliar.caipora.util.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class DeployServiceImpl implements DeployService {

    private final DeployRepository deployRepository;
    private final VersionRepository versionRepository;
    private final UserRepository userRepository;
    private final StakeholderRepository stakeholderRepository;
    private final DeployMapper deployMapper;

    public DeployServiceImpl(final DeployRepository deployRepository,
            final VersionRepository versionRepository, final UserRepository userRepository,
            final StakeholderRepository stakeholderRepository, final DeployMapper deployMapper) {
        this.deployRepository = deployRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.deployMapper = deployMapper;
    }

    @Override
    public Page<DeployDTO> findAll(final String filter, final Pageable pageable) {
        Page<Deploy> page;
        if (filter != null) {
            Integer intFilter = null;
            try {
                intFilter = Integer.parseInt(filter);
            } catch (final NumberFormatException numberFormatException) {
                // keep null - no parseable input
            }
            page = deployRepository.findAllById(intFilter, pageable);
        } else {
            page = deployRepository.findAll(pageable);
        }
        return new PageImpl<>(page.getContent()
                .stream()
                .map(deploy -> deployMapper.updateDeployDTO(deploy, new DeployDTO()))
                .toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public DeployDTO get(final Integer id) {
        return deployRepository.findById(id)
                .map(deploy -> deployMapper.updateDeployDTO(deploy, new DeployDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public Integer create(final DeployDTO deployDTO) {
        final Deploy deploy = new Deploy();
        deployMapper.updateDeploy(deployDTO, deploy, versionRepository, userRepository, stakeholderRepository);
        return deployRepository.save(deploy).getId();
    }

    @Override
    public void update(final Integer id, final DeployDTO deployDTO) {
        final Deploy deploy = deployRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        deployMapper.updateDeploy(deployDTO, deploy, versionRepository, userRepository, stakeholderRepository);
        deployRepository.save(deploy);
    }

    @Override
    public void delete(final Integer id) {
        deployRepository.deleteById(id);
    }

    @Override
    public boolean rfcExists(final String rfc) {
        return deployRepository.existsByRfcIgnoreCase(rfc);
    }

}
