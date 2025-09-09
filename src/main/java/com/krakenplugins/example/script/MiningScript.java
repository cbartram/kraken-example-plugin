package com.krakenplugins.example.script;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.Context;
import com.kraken.api.core.script.BehaviorNode;
import com.kraken.api.core.script.BehaviorTreeScript;
import com.krakenplugins.example.script.actions.*;
import com.krakenplugins.example.script.conditions.AtBankCondition;
import com.krakenplugins.example.script.conditions.InMiningAreaCondition;
import com.krakenplugins.example.script.conditions.InventoryFullCondition;
import com.krakenplugins.example.script.conditions.IsMiningCondition;
import com.krakenplugins.example.script.factory.RepeatNodeFactory;
import com.krakenplugins.example.script.factory.SelectorNodeFactory;
import com.krakenplugins.example.script.factory.SequenceNodeFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.coords.WorldPoint;

import java.util.List;

import static com.krakenplugins.example.script.Util.getRandomDelay;

@Slf4j
@Singleton
public class MiningScript extends BehaviorTreeScript {

    public static final int IRON_ORE_ID = 440;
    public static final WorldPoint MINING_AREA = new WorldPoint(3287, 3367, 0);
    public static final WorldPoint BANK_AREA = new WorldPoint(3253, 3420, 0); // Varrock East bank

    private final InventoryFullCondition inventoryFullCondition;
    private final AtBankCondition atBankCondition;
    private final BankOreAction bankOreAction;
    private final WalkToBankAction walkToBankAction;
    private final InMiningAreaCondition inMiningAreaCondition;
    private final IsMiningCondition isMiningCondition;
    private final WaitAction waitAction;
    private final ClickRockAction clickRockAction;
    private final WalkToMiningAreaAction walkToMiningAreaAction;
    private final FallbackWaitAction fallbackWaitAction;
    private final SequenceNodeFactory sequenceFactory;
    private final SelectorNodeFactory selectorFactory;
    private final RepeatNodeFactory repeatFactory;

    @Getter
    private final ScriptContext scriptContext;

    @Inject
    public MiningScript(Context context, ScriptContext scriptContext, InventoryFullCondition inventoryFullCondition, AtBankCondition atBankCondition, BankOreAction bankOreAction, WalkToBankAction walkToBankAction, InMiningAreaCondition inMiningAreaCondition, IsMiningCondition isMiningCondition, WaitAction waitAction,
                        ClickRockAction clickRockAction, WalkToMiningAreaAction walkToMiningAreaAction, FallbackWaitAction fallbackWaitAction,
                        SequenceNodeFactory sequenceFactory, SelectorNodeFactory selectorFactory, RepeatNodeFactory repeatFactory) {
        super(context);
        this.scriptContext = scriptContext;
        this.inventoryFullCondition = inventoryFullCondition;
        this.atBankCondition = atBankCondition;
        this.bankOreAction = bankOreAction;
        this.walkToBankAction = walkToBankAction;
        this.inMiningAreaCondition = inMiningAreaCondition;
        this.isMiningCondition = isMiningCondition;
        this.waitAction = waitAction;
        this.clickRockAction = clickRockAction;
        this.walkToMiningAreaAction = walkToMiningAreaAction;
        this.fallbackWaitAction = fallbackWaitAction;
        this.sequenceFactory = sequenceFactory;
        this.selectorFactory = selectorFactory;
        this.repeatFactory = repeatFactory;
    }

    @Override
    protected BehaviorNode buildBehaviorTree() {
        return selectorFactory.create(List.of(
                // Priority 1: Handle full inventory
                sequenceFactory.create(List.of(
                        inventoryFullCondition,
                        selectorFactory.create(List.of(
                                sequenceFactory.create(List.of(
                                        atBankCondition,
                                        repeatFactory.create(bankOreAction, 1)
                                )),
                                walkToBankAction
                        ))
                )),

                // Priority 2: Mine iron ore
                sequenceFactory.create(List.of(
                        inMiningAreaCondition,
                        selectorFactory.create(List.of(
                                sequenceFactory.create(List.of(
                                        isMiningCondition,
                                        waitAction
                                )),
                                sequenceFactory.create(List.of(
                                        clickRockAction
                                ))
                        ))
                )),

                // Priority 3: Walk to mining area
                walkToMiningAreaAction,

                // Fallback
                fallbackWaitAction
        ));
    }

    @Override
    protected void onBehaviorTreeStart() {}

    @Override
    protected long onBehaviorTreeSuccess() {
        return getRandomDelay(1000, 3000);
    }

    @Override
    protected long onBehaviorTreeFailure() {
        return getRandomDelay(500, 1000);
    }

    @Override
    protected long onBehaviorTreeRunning() {
        return getRandomDelay(2000, 3000);
    }

    @Override
    protected long getDefaultLoopDelay() {
        return getRandomDelay(100, 300);
    }
}
