/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.cli.commands.parts;

import com.docdoku.api.models.PartCreationDTO;
import com.docdoku.api.models.PartIterationDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.PartsApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.FileHelper;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 * @author Morgan Guimard
 */
public class PartCreationCommand extends BaseCommandLine {

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", required = true, usage = "the part number of the part to save")
    private String partNumber;

    @Option(metaVar = "<partname>", name = "-N", aliases = "--partname", usage = "the part name of the part to save")
    private String partName;

    @Option(metaVar = "<description>", name = "-d", aliases = "--description", usage = "the description of the part to save")
    private String description;

    @Option(name = "-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage = "workspace on which operations occur")
    protected String workspace;

    @Option(name = "-s", aliases = "--standard", metaVar = "<format>", usage = "save as standard part")
    protected boolean standardPart = false;

    @Argument(metaVar = "<cadfile>", required = true, index = 0, usage = "specify the cad file of the part to import")
    private File cadFile;

    @Override
    public void execImpl() throws Exception {

        PartsApi partsApi = new PartsApi(client);
        PartCreationDTO partCreationDTO = new PartCreationDTO();
        partCreationDTO.setDescription(description);
        partCreationDTO.setWorkspaceId(workspace);
        partCreationDTO.setName(partName);
        partCreationDTO.setNumber(partNumber);
        partCreationDTO.setStandardPart(standardPart);
        PartRevisionDTO pr = partsApi.createNewPart(workspace, partCreationDTO);

        PartIterationDTO lastIteration = LastIterationHelper.getLastIteration(pr);
        PartRevisionDTO partRPK = new PartRevisionDTO();
        partRPK.setWorkspaceId(workspace);
        partRPK.setNumber(partNumber);
        partRPK.setVersion(pr.getVersion());

        PartIterationDTO partIPK = new PartIterationDTO();
        partIPK.setWorkspaceId(workspace);
        partIPK.setNumber(partNumber);
        partIPK.setVersion(pr.getVersion());
        partIPK.setIteration(lastIteration.getIteration());

        FileHelper fh = new FileHelper(user, password, output, langHelper);
        fh.uploadNativeCADFile(getServerURL(), cadFile, partIPK);
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("PartCreationCommandDescription");
    }
}
