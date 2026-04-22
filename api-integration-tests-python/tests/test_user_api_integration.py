import json
import http.client
import os
import time
import unittest
import urllib.error
import urllib.request
from random import randint


USER_BASE_URL = os.getenv("USER_API_BASE_URL", os.getenv("API_BASE_URL", "http://localhost:8090"))
STOCK_BASE_URL = os.getenv("STOCK_API_BASE_URL", "http://localhost:8082")
USER_API = f"{USER_BASE_URL}/api/user"
STOCK_API = f"{STOCK_BASE_URL}/api/stock/slots"


def random_user():
    suffix = f"{int(time.time() * 1000)}_{randint(10000, 99999)}"
    return {
        "username": f"it_user_{suffix}",
        "password": "P@ssw0rd123",
        "nickname": f"IT-{suffix}",
    }


def request_json(url, method="GET", payload=None, headers=None):
    body = None if payload is None else json.dumps(payload).encode("utf-8")
    req_headers = {"Content-Type": "application/json"}
    if headers:
        req_headers.update(headers)

    retryable_errors = (
        urllib.error.URLError,
        ConnectionResetError,
        ConnectionAbortedError,
        http.client.RemoteDisconnected,
    )

    for attempt in range(5):
        request = urllib.request.Request(url=url, data=body, method=method, headers=req_headers)
        try:
            with urllib.request.urlopen(request, timeout=10) as response:
                content = response.read().decode("utf-8") if response.length != 0 else ""
                return response.status, json.loads(content) if content else None
        except urllib.error.HTTPError as http_error:
            content = http_error.read().decode("utf-8")
            parsed = json.loads(content) if content else None
            return http_error.code, parsed
        except retryable_errors:
            if attempt == 4:
                raise
            time.sleep(1)


class UserApiIntegrationTest(unittest.TestCase):
    def _request_user(self, path, method="GET", payload=None, headers=None):
        return request_json(f"{USER_API}{path}", method=method, payload=payload, headers=headers)

    def _request_stock(self, path, method="GET", payload=None, headers=None):
        return request_json(f"{STOCK_API}{path}", method=method, payload=payload, headers=headers)

    def _register_and_login(self):
        user = random_user()
        register_status, register_data = self._request_user("/register", method="POST", payload=user)
        self.assertEqual(register_status, 201, msg=f"注册失败: status={register_status}, body={register_data}")
        login_status, login_data = self._request_user(
            "/login",
            method="POST",
            payload={"username": user["username"], "password": user["password"]},
        )
        self.assertEqual(login_status, 200, msg=f"登录失败: status={login_status}, body={login_data}")
        token = login_data["data"].get("accessToken")
        self.assertTrue(token, "登录返回缺少 accessToken")
        return user, register_data["data"]["id"], token

    def test_register_login_me_logout_flow(self):
        user, _, token = self._register_and_login()

        me_status, me_data = self._request_user("/me", method="GET", headers={"Authorization": f"Bearer {token}"})
        self.assertEqual(me_status, 200, msg=f"/me 访问失败: status={me_status}, body={me_data}")
        self.assertTrue(me_data.get("success"))
        self.assertEqual(me_data["data"]["username"], user["username"])

        logout_status, logout_data = self._request_user(
            "/logout",
            method="POST",
            headers={"Authorization": f"Bearer {token}"},
        )
        self.assertEqual(logout_status, 200, msg=f"logout 失败: status={logout_status}, body={logout_data}")
        self.assertTrue(logout_data.get("success"))

        me_again_status, me_again_data = self._request_user(
            "/me",
            method="GET",
            headers={"Authorization": f"Bearer {token}"},
        )
        self.assertEqual(me_again_status, 401, msg=f"退出后 token 仍可用: {me_again_data}")
        self.assertEqual(me_again_data["error"]["code"], "AUTH_TOKEN_INVALID")

    def test_register_should_reject_duplicate_username(self):
        user = random_user()
        first_status, _ = self._request_user("/register", method="POST", payload=user)
        self.assertEqual(first_status, 201)
        second_status, second_data = self._request_user("/register", method="POST", payload=user)
        self.assertEqual(second_status, 409, msg=f"重复用户名未返回409: {second_data}")
        self.assertEqual(second_data["error"]["code"], "USER_USERNAME_EXISTS")

    def test_register_invalid_param_should_be_400(self):
        bad_status, bad_data = self._request_user(
            "/register",
            method="POST",
            payload={"username": "bad!", "password": "123", "nickname": "x"},
        )
        self.assertEqual(bad_status, 400, msg=f"非法注册参数未返回400: {bad_data}")
        self.assertEqual(bad_data["error"]["code"], "REQ_INVALID_PARAM")

    def test_login_with_wrong_password_should_be_401(self):
        user = random_user()
        self._request_user("/register", method="POST", payload=user)
        bad_login_status, bad_login_data = self._request_user(
            "/login",
            method="POST",
            payload={"username": user["username"], "password": "bad-password"},
        )
        self.assertEqual(bad_login_status, 401, msg=f"错误密码登录未返回 401: status={bad_login_status}, body={bad_login_data}")
        self.assertFalse(bad_login_data.get("success"))
        self.assertEqual(bad_login_data["error"]["code"], "AUTH_BAD_CREDENTIALS")

    def test_me_without_token_should_be_401(self):
        status, data = self._request_user("/me", method="GET")
        self.assertEqual(status, 401, msg=f"未带token访问/me未返回401: {data}")
        self.assertEqual(data["error"]["code"], "AUTH_TOKEN_MISSING")

    def test_admin_ping_normal_user_should_be_403(self):
        _, _, token = self._register_and_login()
        status, data = self._request_user("/admin/ping", method="GET", headers={"Authorization": f"Bearer {token}"})
        self.assertEqual(status, 403, msg=f"普通用户访问admin接口应403: {data}")
        self.assertEqual(data["error"]["code"], "AUTH_FORBIDDEN")

    def test_user_crud_and_balance_flow(self):
        user, user_id, _ = self._register_and_login()

        query_status, query_data = self._request_user(f"/{user_id}", method="GET")
        self.assertEqual(query_status, 200)
        self.assertTrue(query_data.get("success"))
        self.assertEqual(query_data["data"]["username"], user["username"])

        update_status, update_data = self._request_user(
            f"/{user_id}",
            method="PUT",
            payload={"nickname": "UpdatedNick"},
        )
        self.assertEqual(update_status, 200, msg=f"更新用户失败: {update_data}")
        self.assertTrue(update_data.get("success"))
        self.assertEqual(update_data["data"]["nickname"], "UpdatedNick")

        recharge_status, recharge_data = self._request_user(
            f"/{user_id}/recharge",
            method="POST",
            payload={"amount": "25.50"},
        )
        self.assertEqual(recharge_status, 200, msg=f"充值失败: {recharge_data}")
        self.assertTrue(recharge_data.get("success"))

        deduct_status, deduct_data = self._request_user(
            f"/{user_id}/deduct",
            method="POST",
            payload={"amount": "10.00"},
        )
        self.assertEqual(deduct_status, 200, msg=f"扣款失败: {deduct_data}")
        self.assertTrue(deduct_data.get("success"))

        delete_status, delete_data = self._request_user(f"/{user_id}", method="DELETE")
        self.assertEqual(delete_status, 200, msg=f"删除用户失败: {delete_data}")
        self.assertTrue(delete_data.get("success"))

        query_deleted_status, query_deleted_data = self._request_user(f"/{user_id}", method="GET")
        self.assertEqual(
            query_deleted_status,
            200,
            msg=f"删除后查询接口异常: status={query_deleted_status}, body={query_deleted_data}",
        )
        self.assertFalse(query_deleted_data.get("success"))
        self.assertIsNone(query_deleted_data.get("data"))

    def test_stock_slot_preheat_should_support_overwrite_and_validation(self):
        redis_key = f"stock:test:{int(time.time() * 1000)}"

        first_status, first_data = self._request_stock(
            "/preheat",
            method="POST",
            payload={"slotId": "A", "redisKey": redis_key, "quantity": 7, "overwrite": False},
        )
        self.assertEqual(first_status, 200, msg=f"首次预热失败: {first_data}")
        self.assertTrue(first_data.get("written"))
        self.assertEqual(first_data.get("currentValue"), "7")

        second_status, second_data = self._request_stock(
            "/preheat",
            method="POST",
            payload={"slotId": "A", "redisKey": redis_key, "quantity": 9, "overwrite": False},
        )
        self.assertEqual(second_status, 200, msg=f"二次预热失败: {second_data}")
        self.assertFalse(second_data.get("written"))
        self.assertEqual(second_data.get("currentValue"), "7")

        overwrite_status, overwrite_data = self._request_stock(
            "/preheat",
            method="POST",
            payload={"slotId": "A", "redisKey": redis_key, "quantity": 11, "overwrite": True},
        )
        self.assertEqual(overwrite_status, 200, msg=f"覆盖预热失败: {overwrite_data}")
        self.assertTrue(overwrite_data.get("written"))
        self.assertEqual(overwrite_data.get("currentValue"), "11")

        invalid_status, invalid_data = self._request_stock(
            "/preheat",
            method="POST",
            payload={"slotId": "A", "redisKey": redis_key, "quantity": 0, "overwrite": True},
        )
        self.assertEqual(invalid_status, 400, msg=f"非法预热参数未返回400: {invalid_data}")
        self.assertIn("message", invalid_data)

if __name__ == "__main__":
    unittest.main()
