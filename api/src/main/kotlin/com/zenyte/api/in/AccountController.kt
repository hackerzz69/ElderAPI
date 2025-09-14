package com.zenyte.api.`in`

import com.zenyte.api.model.*
import com.zenyte.sql.query.adventurers.SubmitAwardQuery
import com.zenyte.sql.query.adventurers.SubmitGameLogQuery
import com.zenyte.sql.query.store.CheckStoreQuery
import com.zenyte.sql.query.store.ClaimBondQuery
import com.zenyte.sql.query.store.TotalDonatedQuery
import com.zenyte.sql.query.user.CheckVoteQuery
import com.zenyte.sql.query.user.LoginUserQuery
import com.zenyte.sql.query.user.RegisterUserQuery
import org.springframework.web.bind.annotation.*

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
@RestController
@RequestMapping("/account")
class AccountController {
    
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): String {
        return (LoginUserQuery(request).getResults().first as LoginUserQuery.LoginResult).successful
    }
    
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): String {
        return (RegisterUserQuery(request).getResults().first as RegisterUserQuery.RegisterResult).response
    }

    @PostMapping("/submitGamelog")
    fun submitGamelog(@RequestBody request: SubmitGamelogRequest): Boolean {
        return (SubmitGameLogQuery(request).getResults().first as SubmitGameLogQuery.SubmitGameLogResult).success
    }

    @PostMapping("/submitAward")
    fun submitAward(@RequestBody request: SubmitAwardRequest): Boolean {
        return (SubmitAwardQuery(request).getResults().first as SubmitAwardQuery.SubmitAwardResult).successful
    }

    @GetMapping("/vote/{displayName}")
    fun vote(@PathVariable displayName: String): Int {
        val results = CheckVoteQuery(displayName).getResults().first as CheckVoteQuery.VoteCheckResults
        return if(results.amount != null) results.amount else 0
    }

    @GetMapping("/donate/{displayName}")
    fun donate(@PathVariable displayName: String): List<StorePurchase>? {
        val results = CheckStoreQuery(displayName).getResults().first as CheckStoreQuery.DonationResult
        return results.items
    }

    @PostMapping("/bond")
    fun claimBond(@RequestBody request: ClaimBondRequest): Boolean {
        return (ClaimBondQuery(request).getResults().first as ClaimBondQuery.BondClaimResponse).success
    }

    @GetMapping("/spent/{displayName}")
    fun donatedAmount(@PathVariable displayName: String): Int {
        return (TotalDonatedQuery(displayName).getResults().first as TotalDonatedQuery.TotalDonatedResult).total
    }
    
}
