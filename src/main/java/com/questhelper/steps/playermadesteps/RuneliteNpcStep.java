/*
 * Copyright (c) 2023, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.questhelper.steps.playermadesteps;

import com.questhelper.QuestHelperPlugin;
import com.questhelper.questhelpers.QuestHelper;
import com.questhelper.requirements.Requirement;
import com.questhelper.steps.DetailedQuestStep;
import com.questhelper.steps.overlay.DirectionArrow;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.ui.overlay.OverlayUtil;

// TODO: Separate out NPC logic from Step logic
public class RuneliteNpcStep extends DetailedQuestStep
{
	@Inject
	private RuneliteObjectManager runeliteObjectManager;

	private final RuneliteNpc runeliteNpc;

	// TODO: Maybe a list of npcs to 'delete' with this?
	List<String> npcsToDelete = new ArrayList<>();

	public RuneliteNpcStep(QuestHelper questHelper, RuneliteNpc runeliteNpc, WorldPoint wp, String text, Requirement... requirements)
	{
		super(questHelper, wp, text, requirements);
		this.runeliteNpc = runeliteNpc;
	}

	@Override
	public void startUp()
	{
		super.startUp();
	}

	@Override
	public void shutDown()
	{
		super.shutDown();
		// Delete all fake npcs associated
		clientThread.invokeLater(this::removeRuneliteNpc);
	}

	private void removeRuneliteNpc()
	{
		for (String npcGroupID : npcsToDelete)
		{
			runeliteObjectManager.remove(npcGroupID);
		}
	}

	public void addNpcToDelete(String id)
	{
		npcsToDelete.add(id);
	}

	@Override
	public void renderArrow(Graphics2D graphics)
	{
		if (questHelper.getConfig().showMiniMapArrow())
		{
			if (!runeliteNpc.isActive())
			{
				super.renderArrow(graphics);
			}
			else if (!hideWorldArrow)
			{
				Point p = Perspective.localToCanvas(client, runeliteNpc.getRuneliteObject().getLocation(), client.getPlane(),
					runeliteNpc.getRuneliteObject().getModelHeight());
				if (p != null)
				{
					DirectionArrow.drawWorldArrow(graphics, getQuestHelper().getConfig().targetOverlayColor(), p.getX(), p.getY() - ARROW_SHIFT_Y);
				}
			}
		}
	}

	@Override
	public void makeWorldOverlayHint(Graphics2D graphics, QuestHelperPlugin plugin)
	{
		super.makeWorldOverlayHint(graphics, plugin);

		Color configColor = getQuestHelper().getConfig().targetOverlayColor();
		highlightNpc(configColor, graphics);
	}

	private void highlightNpc(Color color, Graphics2D graphics)
	{
		switch (questHelper.getConfig().highlightStyleNpcs())
		{
			case CONVEX_HULL:
			case OUTLINE:
				if (!runeliteNpc.getRuneliteObject().isActive()) break;
				modelOutlineRenderer.drawOutline(
					runeliteNpc.getRuneliteObject(),
					questHelper.getConfig().outlineThickness(),
					color,
					questHelper.getConfig().outlineFeathering()
				);
				break;
			case TILE:
				Polygon poly = Perspective.getCanvasTilePoly(client, runeliteNpc.getRuneliteObject().getLocation());
				if (poly != null)
				{
					OverlayUtil.renderPolygon(graphics, poly, color);
				}
				break;
			default:
		}
	}
}