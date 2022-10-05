package com.ivy.formula.domain.source

import com.ivy.core.domain.action.FlowAction
import com.ivy.core.domain.action.calculate.CalculateFlow
import com.ivy.core.domain.action.transaction.TrnsFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataSourceFlow @Inject constructor(
    private val trnsFlow: TrnsFlow,
    private val calculateFlow: CalculateFlow
) : FlowAction<DataSource, Double>() {
    @OptIn(FlowPreview::class)
    override fun DataSource.createFlow(): Flow<Double> = trnsFlow(filter).flatMapMerge { trns ->
        calculateFlow(
            CalculateFlow.Input(
                trns = trns,
                includeTransfers = false,
                includeHidden = false,
                outputCurrency = calculation.outputCurrency
            )
        )
    }.map { stats ->
        val byValue = calculation.type == CalculationType.ByValue
        when (calculation.thing) {
            CalculationThing.Income ->
                if (byValue) stats.income.amount else stats.incomesCount
            CalculationThing.Expense ->
                if (byValue) stats.expense.amount else stats.expensesCount
            CalculationThing.Balance -> if (byValue)
                stats.balance.amount else (stats.incomesCount - stats.expensesCount)
        }.toDouble()
    }
}